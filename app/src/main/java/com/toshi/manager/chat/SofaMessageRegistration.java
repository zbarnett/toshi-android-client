/*
 * 	Copyright (c) 2017. Toshi Inc
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

package com.toshi.manager.chat;


import com.toshi.crypto.signal.ChatService;
import com.toshi.crypto.signal.SignalPreferences;
import com.toshi.crypto.signal.store.ProtocolStore;
import com.toshi.manager.network.IdService;
import com.toshi.model.local.Recipient;
import com.toshi.model.local.User;
import com.toshi.model.network.UserSearchResults;
import com.toshi.util.GcmPrefsUtil;
import com.toshi.util.GcmUtil;
import com.toshi.util.LogUtil;
import com.toshi.util.SharedPrefsUtil;
import com.toshi.view.BaseApplication;

import org.whispersystems.libsignal.util.guava.Optional;

import java.io.IOException;

import rx.Completable;
import rx.schedulers.Schedulers;

public class SofaMessageRegistration {

    private static final String ONBOARDING_BOT_NAME = "ToshiBot";

    private final ChatService chatService;
    private final ProtocolStore protocolStore;

    public SofaMessageRegistration(final ChatService chatService, final ProtocolStore protocolStore) {
        this.chatService = chatService;
        this.protocolStore = protocolStore;

        if (this.chatService == null || this.protocolStore == null) {
            throw new NullPointerException("Initialised with null");
        }
    }

    public Completable registerIfNeeded() {
        if (!SignalPreferences.getRegisteredWithServer()) {
            return this.chatService
                    .registerKeys(this.protocolStore)
                    .andThen(setRegisteredWithServer())
                    .andThen(forceRegisterChatGcm())
                    .doOnCompleted(this::tryTriggerOnboarding);
        } else {
            return registerChatGcm();
        }
    }

    private Completable setRegisteredWithServer() {
        return Completable.fromAction(SignalPreferences::setRegisteredWithServer);
    }

    public Completable forceRegisterChatGcm() {
        GcmPrefsUtil.setChatGcmTokenSentToServer(false);
        return registerChatGcm();
    }

    private Completable registerChatGcm() {
        if (GcmPrefsUtil.isChatGcmTokenSentToServer()) return Completable.complete();
        return GcmUtil.getGcmToken()
                .flatMapCompletable(this::tryRegisterChatGcm);
    }

    private Completable tryRegisterChatGcm(final String token) {
        return Completable.fromAction(() -> {
            try {
                final Optional<String> optional = Optional.of(token);
                this.chatService.setGcmId(optional);
                GcmPrefsUtil.setChatGcmTokenSentToServer(true);
            } catch (IOException e) {
                LogUtil.exception(getClass(), "Error during registering of GCM " + e.getMessage());
                GcmPrefsUtil.setChatGcmTokenSentToServer(false);
                Completable.error(e);
            }
        })
        .subscribeOn(Schedulers.io());
    }

    public Completable tryUnregisterGcm() {
        return Completable.fromAction(() -> {
            try {
                this.chatService.setGcmId(Optional.absent());
                GcmPrefsUtil.setChatGcmTokenSentToServer(false);
            } catch (IOException e) {
                LogUtil.d(getClass(), "Error during unregistering of GCM " + e.getMessage());
                Completable.error(e);
            }
        })
        .subscribeOn(Schedulers.io());
    }

    private void tryTriggerOnboarding() {
        if (SharedPrefsUtil.hasOnboarded()) return;

        IdService.getApi()
                .searchByUsername(ONBOARDING_BOT_NAME)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(UserSearchResults::getResults)
                .toObservable()
                .flatMapIterable(users -> users)
                .filter(user -> user.getUsernameForEditing().equals(ONBOARDING_BOT_NAME))
                .toSingle()
                .subscribe(
                        this::sendOnboardingMessageToOnboardingBot,
                        this::handleOnboardingBotError
                );
    }

    private void sendOnboardingMessageToOnboardingBot(final User onboardingBot) {
        BaseApplication
                .get()
                .getUserManager()
                .getCurrentUser()
                .doOnSuccess(__ -> SharedPrefsUtil.setHasOnboarded(true))
                .subscribe(
                        currentUser -> this.sendOnboardingMessage(currentUser, new Recipient(onboardingBot)),
                        this::handleOnboardingBotError
                );
    }

    private void handleOnboardingBotError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error during sending onboarding message to bot", throwable);
    }

    private void sendOnboardingMessage(final User sender, final Recipient onboardingBot) {
        BaseApplication
                .get()
                .getSofaMessageManager()
                .sendInitMessage(sender, onboardingBot);
    }

    public void clear() {
        GcmPrefsUtil.clear();
    }
}
