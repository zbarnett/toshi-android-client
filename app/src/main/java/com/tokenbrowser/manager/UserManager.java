/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.manager;


import android.content.Context;
import android.content.SharedPreferences;

import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.manager.network.IdService;
import com.tokenbrowser.manager.store.BlockedUserStore;
import com.tokenbrowser.manager.store.ContactStore;
import com.tokenbrowser.manager.store.UserStore;
import com.tokenbrowser.model.local.BlockedUser;
import com.tokenbrowser.model.local.Contact;
import com.tokenbrowser.model.local.Report;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.ServerTime;
import com.tokenbrowser.model.network.UserDetails;
import com.tokenbrowser.model.network.UserSearchResults;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.view.BaseApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.adapter.rxjava.HttpException;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final static String OLD_USER_ID = "uid";
    private final static String USER_ID = "uid_v2";
    private final static String FORM_DATA_NAME = "Profile-Image-Upload";

    private final BehaviorSubject<User> userSubject = BehaviorSubject.create();
    private SharedPreferences prefs;
    private HDWallet wallet;
    private ContactStore contactStore;
    private UserStore userStore;
    private BlockedUserStore blockedUserStore;

    /* package */ UserManager() {
        initDatabases();
    }

    public final BehaviorSubject<User> getUserObservable() {
        return this.userSubject;
    }

    public final Single<User> getCurrentUser() {
        return
                this.userSubject
                .filter(user -> user != null)
                .first()
                .toSingle()
                .doOnError(t -> LogUtil.exception(getClass(), "getCurrentUser", t))
                .onErrorReturn(null);
    }

    public UserManager init(final HDWallet wallet) {
        this.wallet = wallet;
        this.prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        attachConnectivityListener();
        return this;
    }

    private void initDatabases() {
        this.contactStore = new ContactStore();
        this.userStore = new UserStore();
        this.blockedUserStore = new BlockedUserStore();
    }

    private void attachConnectivityListener() {
        // Whenever the network changes init the user.
        // This is dumb and potentially inefficient but it shouldn't have
        // any adverse effects and it is easy to improve later.
        BaseApplication
                .get()
                .isConnectedSubject()
                .subscribe(
                        isConnected -> initUser(),
                        this::handleUserError
                );
    }

    private void initUser() {
        if (userNeedsToRegister()) {
            registerNewUser();
        } else if (userNeedsToMigrate()) {
            migrateUser();
        } else if (SharedPrefsUtil.shouldForceUserUpdate()) {
            forceUpdateUser();
        } else {
            getExistingUser();
        }
    }

    private boolean userNeedsToRegister() {
        final String oldUserId = this.prefs.getString(OLD_USER_ID, null);
        final String newUserId = this.prefs.getString(USER_ID, null);
        final String expectedAddress = this.wallet.getOwnerAddress();
        final String userId = newUserId == null ? oldUserId : newUserId;
        return userId == null || !userId.equals(expectedAddress);
    }

    private boolean userNeedsToMigrate() {
        final String userId = this.prefs.getString(USER_ID, null);
        final String expectedAddress = this.wallet.getOwnerAddress();
        return userId == null || !userId.equals(expectedAddress);
    }

    private void registerNewUser() {
        getTimestamp()
        .subscribe(
                this::registerNewUserWithTimestamp,
                this::handleUserError
        );
    }

    private void handleUserError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Unable to register/fetch user", throwable);
    }

    private void registerNewUserWithTimestamp(final ServerTime serverTime) {
        final UserDetails ud = new UserDetails().setPaymentAddress(this.wallet.getPaymentAddress());

        IdService
            .getApi()
            .registerUser(ud, serverTime.get())
            .subscribe(
                    this::updateCurrentUser,
                    this::handleUserRegistrationFailed
            );
        SharedPrefsUtil.setForceUserUpdate(false);
    }

    private void handleUserRegistrationFailed(final Throwable throwable) {
        LogUtil.error(getClass(), throwable.toString());
        if (throwable instanceof HttpException && ((HttpException)throwable).code() == 400) {
            getExistingUser();
        }
    }

    private void getExistingUser() {
        IdService
            .getApi()
            .getUser(this.wallet.getOwnerAddress())
            .subscribe(
                    this::updateCurrentUser,
                    this::handleUserError
            );
    }

    private void updateCurrentUser(final User user) {
        prefs
            .edit()
            .putString(USER_ID, user.getTokenId())
            .apply();
        this.userSubject.onNext(user);
    }

    private void migrateUser() {
        forceUpdateUser();
        SharedPrefsUtil.setWasMigrated(true);
    }

    private void forceUpdateUser() {
        final UserDetails ud = new UserDetails().setPaymentAddress(this.wallet.getPaymentAddress());
        updateUser(ud).subscribe(
                __ -> {},
                this::handleUserError
        );
        SharedPrefsUtil.setForceUserUpdate(false);
    }

    public Single<User> updateUser(final UserDetails userDetails) {
        return getTimestamp()
                .subscribeOn(Schedulers.io())
                .flatMap(serverTime -> updateUserWithTimestamp(userDetails, serverTime));
    }

    private Single<User> updateUserWithTimestamp(
            final UserDetails userDetails,
            final ServerTime serverTime) {

        return IdService.getApi()
                .updateUser(this.wallet.getOwnerAddress(), userDetails, serverTime.get())
                .subscribeOn(Schedulers.io())
                .doOnSuccess(this::updateCurrentUser);
    }

    public Single<User> getUserFromUsername(final String username) {
        // It's the same endpoint
        return getUserFromTokenId(username);
    }

    public Single<User> getUserFromTokenId(final String tokenId) {
        return Observable
                .concat(
                        this.userStore.loadForTokenId(tokenId),
                        this.fetchAndCacheFromNetworkByTokenId(tokenId))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .first(user -> user != null && !user.needsRefresh())
                .doOnError(t -> LogUtil.exception(getClass(), "getUserFromTokenId", t))
                .toSingle();
    }

    public Single<User> getUserFromPaymentAddress(final String paymentAddress) {
        return Single
                .concat(
                        Single.just(userStore.loadForPaymentAddress(paymentAddress)),
                        this.fetchAndCacheFromNetworkByPaymentAddress(paymentAddress).toSingle()
                )
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .first(user -> user != null && !user.needsRefresh())
                .doOnError(t -> LogUtil.exception(getClass(), "getUserFromPaymentAddress", t))
                .toSingle();

    }

    private Observable<User> fetchAndCacheFromNetworkByTokenId(final String userAddress) {
        return IdService
                .getApi()
                .getUser(userAddress)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(this::cacheUser);
    }

    private Observable<User> fetchAndCacheFromNetworkByPaymentAddress(final String paymentAddress) {
        return IdService
                .getApi()
                .searchByPaymentAddress(paymentAddress)
                .toObservable()
                .filter(userSearchResults -> userSearchResults.getResults().size() > 0)
                .map(userSearchResults -> userSearchResults.getResults().get(0))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(this::cacheUser)
                .doOnError(t -> LogUtil.exception(getClass(), "fetchAndCacheFromNetworkByPaymentAddress", t));
    }

    private void cacheUser(final User user) {
        if (this.userStore == null) {
            return;
        }

        this.userStore.save(user);
    }

    public Single<List<Contact>> loadAllContacts() {
        return this.contactStore
                .loadAll()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    public Single<List<User>> searchOfflineUsers(final String query) {
        return this.userStore
                .queryUsername(query)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    public Single<List<User>> searchOnlineUsers(final String query) {
        return IdService
                .getApi()
                .searchByUsername(query)
                .subscribeOn(Schedulers.io())
                .map(UserSearchResults::getResults);
    }

    public Single<Void> webLogin(final String loginToken) {
        return getTimestamp()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap((serverTime) -> webLoginWithTimestamp(loginToken, serverTime));
    }

    private Single<Void> webLoginWithTimestamp(final String loginToken, final ServerTime serverTime) {
        if (serverTime == null) {
            throw new IllegalStateException("ServerTime was null");
        }

        return IdService
                .getApi()
                .webLogin(loginToken, serverTime.get());
    }

    public Single<Boolean> isUserAContact(final User user) {
        return this.contactStore.userIsAContact(user);
    }

    public Completable deleteContact(final User user) {
        return this.contactStore.delete(user);
    }

    public Completable saveContact(final User user) {
        return this.contactStore.save(user);
    }

    public Single<Boolean> isUserBlocked(final String ownerAddress) {
        return this.blockedUserStore
                .isBlocked(ownerAddress)
                .subscribeOn(Schedulers.io());
    }

    public Completable blockUser(final String ownerAddress) {
        final BlockedUser blockedUser = new BlockedUser()
                .setOwnerAddress(ownerAddress);
        return Completable.fromAction(() ->
                this.blockedUserStore.save(blockedUser))
                .subscribeOn(Schedulers.io());
    }

    public Completable unblockUser(final String ownerAddress) {
        return Completable.fromAction(() ->
                this.blockedUserStore.delete(ownerAddress))
                .subscribeOn(Schedulers.io());
    }

    public Single<Void> reportUser(final Report report) {
        return getTimestamp()
                .flatMap(serverTime ->
                        IdService
                        .getApi()
                        .reportUser(report, serverTime.get())
                )
                .subscribeOn(Schedulers.io());
    }

    public Single<User> uploadAvatar(final File file) {
        final FileUtil fileUtil = new FileUtil();
        final String mimeType = fileUtil.getMimeTypeFromFilename(file.getName());
        if (mimeType == null) {
            return Single.error(new IllegalArgumentException("Unable to determine file type from file."));
        }
        final MediaType mediaType = MediaType.parse(mimeType);
        final RequestBody requestFile = RequestBody.create(mediaType, file);
        final MultipartBody.Part body = MultipartBody.Part.createFormData(FORM_DATA_NAME, file.getName(), requestFile);

        return getTimestamp()
                .subscribeOn(Schedulers.io())
                .flatMap(serverTime -> IdService
                        .getApi()
                        .uploadFile(body, serverTime.get()))
                .doOnSuccess(this.userSubject::onNext);
    }

    public Single<ServerTime> getTimestamp() {
        return IdService
                .getApi()
                .getTimestamp();
    }

    public void clear() {
        clearCache();
        clearUserId();
    }

    private void clearUserId() {
        this.prefs
                .edit()
                .putString(USER_ID, null)
                .apply();
        this.userSubject.onNext(null);
    }

    private void clearCache() {
        try {
            IdService
                    .get()
                    .clearCache();
        } catch (IOException e) {
            LogUtil.exception(getClass(), "Error while clearing network cache", e);
        }
    }
}
