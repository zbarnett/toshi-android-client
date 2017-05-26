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

import com.tokenbrowser.model.local.Conversation;
import com.tokenbrowser.model.sofa.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.LogUtil;

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

public class ConversationStore {

    private static final int FIFTEEN_MINUTES = 1000 * 60 * 15;

    private static String watchedConversationId;
    private final static PublishSubject<SofaMessage> newMessageObservable = PublishSubject.create();
    private final static PublishSubject<SofaMessage> updatedMessageObservable = PublishSubject.create();
    private final static PublishSubject<Conversation> conversationChangedObservable = PublishSubject.create();
    private final static ExecutorService dbThread = Executors.newSingleThreadExecutor();

    // Returns a pair of RxSubjects, the first being the observable for new messages
    // the second being the observable for updated messages.
    public Pair<PublishSubject<SofaMessage>, PublishSubject<SofaMessage>> registerForChanges(final String conversationId) {
        watchedConversationId = conversationId;
        return new Pair<>(newMessageObservable, updatedMessageObservable);
    }

    public void stopListeningForChanges() {
        watchedConversationId = null;
    }

    public Observable<Conversation> getConversationChangedObservable() {
        return conversationChangedObservable
                .filter(conversation -> conversation != null);
    }

    public void saveNewMessage(final User user, final SofaMessage message) {
        saveMessage(user, message)
        .observeOn(Schedulers.immediate())
        .subscribeOn(Schedulers.from(dbThread))
        .subscribe(
                conversationForBroadcast -> {
                    broadcastNewChatMessage(user.getTokenId(), message);
                    broadcastConversationChanged(conversationForBroadcast);
                },
                this::handleError
        );
    }

    private Single<Conversation> saveMessage(final User user, final SofaMessage message) {
        return Single.fromCallable(() -> {
            final Conversation existingConversation = loadWhere("conversationId", user.getTokenId());
            final Conversation conversationToStore = existingConversation == null
                    ? new Conversation(user)
                    : existingConversation;

            if (shouldSaveTimestampMessage(message, conversationToStore)) {
                final SofaMessage timestamMessage =
                        generateTimestampMessage();
                conversationToStore.addMessage(timestamMessage);
                broadcastNewChatMessage(user.getTokenId(), timestamMessage);
            }

            final Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            final SofaMessage storedMessage = realm.copyToRealmOrUpdate(message);
            conversationToStore.setLatestMessage(storedMessage);

            conversationToStore.setNumberOfUnread(calculateNumberOfUnread(conversationToStore));
            final Conversation storedConversation = realm.copyToRealmOrUpdate(conversationToStore);
            realm.commitTransaction();
            final Conversation conversationForBroadcast = realm.copyFromRealm(storedConversation);
            realm.close();

            return conversationForBroadcast;
        });
    }

    private SofaMessage generateTimestampMessage() {
        return new SofaMessage().makeNewTimeStampMessage();
    }

    private boolean shouldSaveTimestampMessage(final SofaMessage message,
                                               final Conversation conversation) {
        final long newMessageTimestamp = message.getCreationTime();
        final long latestMessageTimestamp = conversation.getUpdatedTime();
        return newMessageTimestamp - latestMessageTimestamp > FIFTEEN_MINUTES;
    }

    private int calculateNumberOfUnread(final Conversation conversationToStore) {
        // If we are watching the current conversation the message is automatically read.
        if (   conversationToStore == null
            || conversationToStore.getMember() == null
            || conversationToStore.getMember().getTokenId() == null
            || conversationToStore.getMember().getTokenId().equals(watchedConversationId)) {
            return 0;
        }
        final int currentNumberOfUnread = conversationToStore.getNumberOfUnread();
        return currentNumberOfUnread + 1;
    }

    private void resetUnreadMessageCounter(final String conversationId) {
        Single.fromCallable(() -> {
            final Conversation storedConversation = loadWhere("conversationId", conversationId);
            if (storedConversation == null) {
                return null;
            }

            final Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            storedConversation.setNumberOfUnread(0);
            realm.insertOrUpdate(storedConversation);
            realm.commitTransaction();
            realm.close();
            return storedConversation;
        })
        .observeOn(Schedulers.immediate())
        .subscribeOn(Schedulers.from(dbThread))
        .subscribe(
                this::broadcastConversationChanged,
                this::handleError
        );
    }

    public List<Conversation> loadAll() {
        final Realm realm = Realm.getDefaultInstance();
        final RealmQuery<Conversation> query = realm.where(Conversation.class);
        final RealmResults<Conversation> results = query.findAllSorted("updatedTime", Sort.DESCENDING);
        final List<Conversation> retVal = realm.copyFromRealm(results);
        realm.close();
        return retVal;
    }

    private void broadcastConversationChanged(final Conversation conversation) {
        conversationChangedObservable.onNext(conversation);
    }

    public Conversation loadByAddress(final String address) {
        resetUnreadMessageCounter(address);
        return loadWhere("conversationId", address);
    }

    private Conversation loadWhere(final String fieldName, final String value) {
        final Realm realm = Realm.getDefaultInstance();
        final Conversation result = realm
                .where(Conversation.class)
                .equalTo(fieldName, value)
                .findFirst();
        final Conversation retVal = result == null ? null : realm.copyFromRealm(result);
        realm.close();
        return retVal;
    }

    public void updateMessage(final User user, final SofaMessage message) {
        Single.fromCallable(() -> {
            final Realm realm = Realm.getDefaultInstance();
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
        final Realm realm;
        try {
            realm = Realm.getDefaultInstance();
        } catch (final IllegalStateException ex) {
            LogUtil.exception(getClass(), "RealmConfig unexpectedly null", ex);
            return false;
        }

        final Conversation result = realm
                .where(Conversation.class)
                .greaterThan("numberOfUnread", 0)
                .findFirst();
        final boolean areUnreadMessages = result != null;
        realm.close();
        return areUnreadMessages;
    }

    private void broadcastNewChatMessage(final String conversationId, final SofaMessage newMessage) {
        if (watchedConversationId == null || !watchedConversationId.equals(conversationId)) {
            return;
        }
        newMessageObservable.onNext(newMessage);
    }

    private void broadcastUpdatedChatMessage(final String conversationId, final SofaMessage updatedMessage) {
        if (watchedConversationId == null || !watchedConversationId.equals(conversationId)) {
            return;
        }
        updatedMessageObservable.onNext(updatedMessage);
    }

    private void handleError(final Throwable throwable) {
        LogUtil.exception(getClass(), throwable);
    }
}
