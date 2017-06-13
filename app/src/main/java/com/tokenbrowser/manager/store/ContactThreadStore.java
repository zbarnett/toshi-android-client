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

package com.tokenbrowser.manager.store;


import android.util.Pair;

import com.tokenbrowser.model.local.ContactThread;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.SofaMessage;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class ContactThreadStore {

    private static final int FIFTEEN_MINUTES = 1000 * 60 * 15;
    private static final String THREAD_ID_FIELD = "threadId";

    private static String watchedThreadId;
    private final static PublishSubject<SofaMessage> newMessageObservable = PublishSubject.create();
    private final static PublishSubject<SofaMessage> updatedMessageObservable = PublishSubject.create();
    private final static PublishSubject<ContactThread> threadChangedObservable = PublishSubject.create();
    private final static ExecutorService dbThread = Executors.newSingleThreadExecutor();

    // Returns a pair of RxSubjects, the first being the observable for new messages
    // the second being the observable for updated messages.
    public Pair<PublishSubject<SofaMessage>, PublishSubject<SofaMessage>> registerForChanges(final String threadId) {
        watchedThreadId = threadId;
        return new Pair<>(newMessageObservable, updatedMessageObservable);
    }

    public void stopListeningForChanges() {
        watchedThreadId = null;
    }

    public Observable<ContactThread> getThreadChangedObservable() {
        return threadChangedObservable
                .filter(thread -> thread != null);
    }

    public void saveNewMessage(final User user, final SofaMessage message) {
        saveMessage(user, message)
        .observeOn(Schedulers.immediate())
        .subscribeOn(Schedulers.from(dbThread))
        .subscribe(
                threadForBroadcast -> {
                    broadcastNewChatMessage(user.getTokenId(), message);
                    broadcastThreadChanged(threadForBroadcast);
                },
                this::handleError
        );
    }

    private Single<ContactThread> saveMessage(final User user, final SofaMessage message) {
        return Single.fromCallable(() -> {
            final ContactThread existingContactThread = loadWhere(THREAD_ID_FIELD, user.getTokenId());
            final ContactThread contactThreadToStore = existingContactThread == null
                    ? new ContactThread(user)
                    : existingContactThread;

            if (shouldSaveTimestampMessage(message, contactThreadToStore)) {
                final SofaMessage timestamMessage =
                        generateTimestampMessage();
                contactThreadToStore.addMessage(timestamMessage);
                broadcastNewChatMessage(user.getTokenId(), timestamMessage);
            }

            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            final SofaMessage storedMessage = realm.copyToRealmOrUpdate(message);
            contactThreadToStore.setLatestMessage(storedMessage);

            contactThreadToStore.setNumberOfUnread(calculateNumberOfUnread(contactThreadToStore));
            final ContactThread storedContactThread = realm.copyToRealmOrUpdate(contactThreadToStore);
            realm.commitTransaction();
            final ContactThread contactThreadForBroadcast = realm.copyFromRealm(storedContactThread);
            realm.close();

            return contactThreadForBroadcast;
        });
    }

    private SofaMessage generateTimestampMessage() {
        return new SofaMessage().makeNewTimeStampMessage();
    }

    private boolean shouldSaveTimestampMessage(final SofaMessage message,
                                               final ContactThread contactThread) {
        final long newMessageTimestamp = message.getCreationTime();
        final long latestMessageTimestamp = contactThread.getUpdatedTime();
        return newMessageTimestamp - latestMessageTimestamp > FIFTEEN_MINUTES;
    }

    private int calculateNumberOfUnread(final ContactThread contactThreadToStore) {
        // If we are watching the current thread the message is automatically read.
        if (   contactThreadToStore == null
            || contactThreadToStore.getThreadId().equals(watchedThreadId)) {
            return 0;
        }
        final int currentNumberOfUnread = contactThreadToStore.getNumberOfUnread();
        return currentNumberOfUnread + 1;
    }

    private void resetUnreadMessageCounter(final String threadId) {
        Single.fromCallable(() -> {
            final ContactThread storedContactThread = loadWhere(THREAD_ID_FIELD, threadId);
            if (storedContactThread == null) {
                return null;
            }

            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            storedContactThread.setNumberOfUnread(0);
            realm.insertOrUpdate(storedContactThread);
            realm.commitTransaction();
            realm.close();
            return storedContactThread;
        })
        .observeOn(Schedulers.immediate())
        .subscribeOn(Schedulers.from(dbThread))
        .subscribe(
                this::broadcastThreadChanged,
                this::handleError
        );
    }

    public List<ContactThread> loadAll() {
        final Realm realm = BaseApplication.get().getRealm();
        final RealmQuery<ContactThread> query = realm.where(ContactThread.class);
        final RealmResults<ContactThread> results = query.findAllSorted("updatedTime", Sort.DESCENDING);
        final List<ContactThread> retVal = realm.copyFromRealm(results);
        realm.close();
        return retVal;
    }

    private void broadcastThreadChanged(final ContactThread contactThread) {
        threadChangedObservable.onNext(contactThread);
    }

    public ContactThread loadByAddress(final String address) {
        resetUnreadMessageCounter(address);
        return loadWhere(THREAD_ID_FIELD, address);
    }

    private ContactThread loadWhere(final String fieldName, final String value) {
        final Realm realm = BaseApplication.get().getRealm();
        final ContactThread result = realm
                .where(ContactThread.class)
                .equalTo(fieldName, value)
                .findFirst();
        final ContactThread retVal = result == null ? null : realm.copyFromRealm(result);
        realm.close();
        return retVal;
    }

    public void updateMessage(final User user, final SofaMessage message) {
        Single.fromCallable(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            realm.insertOrUpdate(message);
            realm.commitTransaction();
            realm.close();
            return null;
        })
        .observeOn(Schedulers.immediate())
        .subscribeOn(Schedulers.from(dbThread))
        .subscribe(
                __ -> broadcastUpdatedChatMessage(user.getTokenId(), message),
                this::handleError
        );
    }

    public boolean areUnreadMessages() {
        final Realm realm = BaseApplication.get().getRealm();
        final ContactThread result = realm
                .where(ContactThread.class)
                .greaterThan("numberOfUnread", 0)
                .findFirst();
        final boolean areUnreadMessages = result != null;
        realm.close();
        return areUnreadMessages;
    }

    private void broadcastNewChatMessage(final String threadId, final SofaMessage newMessage) {
        if (watchedThreadId == null || !watchedThreadId.equals(threadId)) {
            return;
        }
        newMessageObservable.onNext(newMessage);
    }

    private void broadcastUpdatedChatMessage(final String threadId, final SofaMessage updatedMessage) {
        if (watchedThreadId == null || !watchedThreadId.equals(threadId)) {
            return;
        }
        updatedMessageObservable.onNext(updatedMessage);
    }

    private void handleError(final Throwable throwable) {
        LogUtil.exception(getClass(), throwable);
    }
}
