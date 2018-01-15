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

package com.toshi.manager.store;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.toshi.extensions.SofaMessageUtil;
import com.toshi.model.local.Avatar;
import com.toshi.model.local.Conversation;
import com.toshi.model.local.ConversationObservables;
import com.toshi.model.local.Group;
import com.toshi.model.local.Recipient;
import com.toshi.model.local.User;
import com.toshi.model.sofa.SofaMessage;
import com.toshi.util.LogUtil;
import com.toshi.util.statusMessage.StatusMessageBuilder;
import com.toshi.view.BaseApplication;

import org.jetbrains.annotations.NotNull;
import org.whispersystems.signalservice.api.messages.SignalServiceGroup;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class ConversationStore {

    private static final int FIFTEEN_MINUTES = 1000 * 60 * 15;
    private static final String THREAD_ID_FIELD = "threadId";
    private static final String MESSAGE_ID_FIELD = "privateKey";

    private static String watchedThreadId;
    private final static PublishSubject<SofaMessage> NEW_MESSAGE_SUBJECT = PublishSubject.create();
    private final static PublishSubject<SofaMessage> UPDATED_MESSAGE_SUBJECT = PublishSubject.create();
    private final static PublishSubject<SofaMessage> DELETED_MESSAGE_SUBJECT = PublishSubject.create();
    private final static PublishSubject<Conversation> CONVERSATION_CHANGED_SUBJECT = PublishSubject.create();
    private final static PublishSubject<Conversation> CONVERSATION_UPDATED_SUBJECT = PublishSubject.create();
    private final static ExecutorService dbThread = Executors.newSingleThreadExecutor();


    //##############################################################################################
    // Observables
    //##############################################################################################
    public ConversationObservables registerForChanges(final String threadId) {
        watchedThreadId = threadId;
        return new ConversationObservables(NEW_MESSAGE_SUBJECT, UPDATED_MESSAGE_SUBJECT, CONVERSATION_UPDATED_SUBJECT);
    }

    public Observable<SofaMessage> registerForDeletedMessages(final String threadId) {
        watchedThreadId = threadId;
        return DELETED_MESSAGE_SUBJECT.asObservable();
    }

    public void stopListeningForChanges(final String threadId) {
        // Avoids the race condition where a second activity has already registered
        // before the first activity is destroyed. Thus the first activity can't deregister
        // changes for the second activity.
        if (watchedThreadId != null && watchedThreadId.equals(threadId)) {
            watchedThreadId = null;
        }
    }

    public Observable<Conversation> getConversationChangedObservable() {
        return CONVERSATION_CHANGED_SUBJECT
                .filter(thread -> thread != null);
    }

    //##############################################################################################
    // Creation
    //##############################################################################################

    public Single<Conversation> saveSignalGroup(@NonNull final SignalServiceGroup signalGroup) {
            final Group group = new Group(signalGroup);
            return copyOrUpdateGroup(group)
                    .doOnSuccess(this::broadcastConversationChanged)
                    .doOnError(this::handleError);
    }

    public Single<Conversation> createNewConversationFromGroup(@NonNull final Group group) {
        return createEmptyConversation(new Recipient(group))
                .flatMap(this::addGroupCreatedStatusMessage)
                .observeOn(Schedulers.immediate())
                .doOnSuccess(this::broadcastConversationChanged)
                .doOnError(this::handleError);
    }

    public Single<Conversation> createEmptyConversation(final Recipient recipient) {
        return Single.fromCallable(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            final Conversation conversation = new Conversation(recipient);
            conversation.getConversationStatus().setAccepted(true);
            realm.copyToRealmOrUpdate(conversation);
            realm.commitTransaction();
            realm.close();
            return conversation;
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    private Single<Conversation> copyOrUpdateGroup(@NonNull final Group group) {
        return Single.fromCallable(() -> {
            final Conversation conversationToStore = getOrCreateConversation(group);
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            conversationToStore.updateRecipient(new Recipient(group));
            final Conversation storedConversation = realm.copyToRealmOrUpdate(conversationToStore);
            realm.commitTransaction();
            final Conversation conversationForBroadcast = realm.copyFromRealm(storedConversation);
            realm.close();
            return conversationForBroadcast;
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    @NonNull
    private Conversation getOrCreateConversation(final Group group) {
        final Recipient recipient = new Recipient(group);
        return getOrCreateConversation(recipient);
    }

    @NonNull
    private Conversation getOrCreateConversation(final Recipient recipient) {
        final Conversation existingConversation = loadWhere(THREAD_ID_FIELD, recipient.getThreadId());
        return existingConversation == null
            ? new Conversation(recipient)
            : existingConversation;
    }

    //##############################################################################################
    // Saving Messages
    //##############################################################################################

    public Single<Conversation> saveNewMessageSingle(@NonNull final Recipient receiver,
                                                     @NonNull final SofaMessage message) {
        return saveMessage(receiver, message)
                .observeOn(Schedulers.immediate())
                .doOnSuccess(this::broadcastConversationChanged)
                .doOnError(throwable -> LogUtil.e(getClass(), "Error while saving message " + throwable));
    }

    public void saveNewMessage(
            @NonNull final Recipient receiver,
            @NonNull final SofaMessage message) {
        saveMessage(receiver, message)
        .observeOn(Schedulers.immediate())
        .subscribe(
                this::broadcastConversationChanged,
                this::handleError
        );
    }

    private Single<Conversation> saveMessage(
        @NonNull final Recipient receiver,
        @Nullable final SofaMessage message) {
        return Single.fromCallable(() -> {
            final Conversation conversationToStore = getOrCreateConversation(receiver);

            if (message != null && shouldSaveTimestampMessage(message, conversationToStore)) {
                final SofaMessage timestampMessage = generateTimestampMessage();
                conversationToStore.addMessage(timestampMessage);
                broadcastNewChatMessage(receiver.getThreadId(), timestampMessage);
            }

            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();

            if (message != null) {
                final SofaMessage storedMessage = realm.copyToRealmOrUpdate(message);
                final boolean updateUnreadCounter = !conversationToStore.getThreadId().equals(watchedThreadId)
                    && !SofaMessageUtil.isLocalStatusMessage(storedMessage);
                if (updateUnreadCounter) conversationToStore.setLatestMessageAndUpdateUnreadCounter(storedMessage);
                else conversationToStore.setLatestMessage(storedMessage);
                broadcastNewChatMessage(receiver.getThreadId(), message);
            }

            final Conversation storedConversation = realm.copyToRealmOrUpdate(conversationToStore);
            realm.commitTransaction();
            final Conversation conversationForBroadcast = realm.copyFromRealm(storedConversation);
            realm.close();

            return conversationForBroadcast;
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    public void updateMessage(final Recipient receiver, final SofaMessage message) {
        Completable.fromAction(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            realm.insertOrUpdate(message);
            realm.commitTransaction();
            realm.close();
        })
        .observeOn(Schedulers.immediate())
        .subscribeOn(Schedulers.from(dbThread))
        .subscribe(
                () -> broadcastUpdatedChatMessage(receiver.getThreadId(), message),
                this::handleError
        );
    }

    private Completable updateLatestMessage(final String threadId) {
        return Completable.fromAction(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            final Conversation conversation = realm
                .where(Conversation.class)
                .equalTo(THREAD_ID_FIELD, threadId)
                .findFirst();
            if (conversation == null) {
                realm.close();
                return;
            }
            if (conversation.getAllMessages() != null && conversation.getAllMessages().size() > 0) {
                final SofaMessage lastMessage = conversation.getAllMessages().get(conversation.getAllMessages().size() - 1);
                realm.beginTransaction();
                conversation.updateLatestMessage(lastMessage);
                realm.commitTransaction();
            }
            realm.close();
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    //##############################################################################################
    // Status Messages
    //##############################################################################################

    public Single<Conversation> addAddedToGroupStatusMessage(@NonNull final Conversation conversation) {
        if (!conversation.getRecipient().isGroup()) throw new IllegalStateException("Adding status message to non-group");
        final SofaMessage statusMessage = StatusMessageBuilder.buildAddedToGroupStatusMessage(conversation.getRecipient().getGroup());
        return saveMessage(conversation.getRecipient(), statusMessage);
    }

    private Single<Conversation> addGroupCreatedStatusMessage(@NonNull final Conversation conversation) {
        final SofaMessage localStatusMessage = StatusMessageBuilder.buildGroupCreatedStatusMessage();
        return saveMessage(conversation.getRecipient(), localStatusMessage);
    }

    private Single<Conversation> addUserLeftStatusMessage(
        @NonNull final Conversation conversation,
        @NonNull User sender) {
        final SofaMessage localStatusMessage = StatusMessageBuilder.buildUserLeftStatusMessage(sender);
        return saveMessage(conversation.getRecipient(), localStatusMessage);
    }

    public Single<Conversation> addNewGroupMembersStatusMessage(
        @NonNull final Recipient recipient,
        @Nullable final User sender,
        @NonNull final List<User> newUsers) {
        final SofaMessage statusMessage = StatusMessageBuilder.buildAddStatusMessage(sender, newUsers);
        if (statusMessage == null) throw new IllegalStateException("Status message is null");
        return saveMessage(recipient, statusMessage);
    }

    public Single<Conversation> addGroupNameUpdatedStatusMessage(
        @NonNull final Recipient recipient,
        @Nullable final User sender,
        @NonNull final String updatedGroupName) {
        final SofaMessage statusMessage = StatusMessageBuilder.addGroupNameUpdatedStatusMessage(sender, updatedGroupName);
        return saveMessage(recipient, statusMessage);
    }

    //##############################################################################################
    // Timestamp
    //##############################################################################################

    private SofaMessage generateTimestampMessage() {
        return new SofaMessage().makeNewTimeStampMessage();
    }

    private boolean shouldSaveTimestampMessage(final SofaMessage message,
                                               final Conversation conversation) {
        if (!message.isUserVisible()) return false;
        final long newMessageTimestamp = message.getCreationTime();
        final long latestMessageTimestamp = conversation.getUpdatedTime();
        return newMessageTimestamp - latestMessageTimestamp > FIFTEEN_MINUTES;
    }

    //##############################################################################################
    // Group Updates
    //##############################################################################################
    public Completable saveGroupAvatar(@NotNull final String groupId, @Nullable final Avatar avatar) {
        return loadByThreadId(groupId)
            .map(conversation -> conversation.getRecipient().getGroup())
            .map(group -> group.setAvatar(avatar))
            .flatMapCompletable(this::saveGroup);
    }

    public Completable saveGroupTitle(@NotNull final String groupId, @NotNull final String title) {
        return loadByThreadId(groupId)
            .map(conversation -> conversation.getRecipient().getGroup())
            .map(group -> group.setTitle(title))
            .flatMapCompletable(this::saveGroup);
    }

    public Completable addNewMembersToGroup(@NotNull final String groupId, @NotNull List<User> newMembers) {
        return loadByThreadId(groupId)
            .map(conversation -> conversation.getRecipient().getGroup())
            .map(group -> group.addMembers(newMembers))
            .flatMapCompletable(this::saveGroup);
    }

    public Completable removeUserFromGroup(@NotNull String groupId, @NotNull User user) {
        return loadByThreadId(groupId)
            .flatMap(conversation -> addUserLeftStatusMessage(conversation, user))
            .map(conversation -> conversation.getRecipient().getGroup())
            .map(group -> group.removeMember(user))
            .flatMapCompletable(this::saveGroup);
    }

    private Completable saveGroup(@NonNull final Group group) {
        return copyOrUpdateGroup(group)
            .observeOn(Schedulers.immediate())
            .doOnSuccess(this::broadcastConversation)
            .doOnError(this::handleError)
            .toCompletable();
    }

    //##############################################################################################
    // Reading from DB
    //##############################################################################################

    public Single<List<Conversation>> loadAllAcceptedConversation() {
        return loadAllConversations(true);
    }

    public Single<List<Conversation>> loadAllUnacceptedConversation() {
        return loadAllConversations(false);
    }

    private Single<List<Conversation>> loadAllConversations(final boolean isAccepted) {
        return Single.fromCallable(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            final RealmQuery<Conversation> query =
                realm.where(Conversation.class)
                    .equalTo("conversationStatus.isAccepted", isAccepted)
                    .isNotEmpty("allMessages");
            final RealmResults<Conversation> results = query.findAllSorted("updatedTime", Sort.DESCENDING);
            final List<Conversation> allConversations = realm.copyFromRealm(results);
            realm.close();
            return allConversations;
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    public Single<Conversation> loadByThreadId(final String threadId) {
        return Single.fromCallable(() -> loadWhere(THREAD_ID_FIELD, threadId))
                .subscribeOn(Schedulers.from(dbThread));
    }

    private Conversation loadWhere(final String fieldName, final String value) {
        final Realm realm = BaseApplication.get().getRealm();
        final Conversation result = realm
            .where(Conversation.class)
            .equalTo(fieldName, value)
            .findFirst();
        final Conversation queriedConversation = result == null ? null : realm.copyFromRealm(result);
        realm.close();
        return queriedConversation;
    }

    public boolean areUnreadMessages() {
        final Realm realm = BaseApplication.get().getRealm();
        final Conversation result = realm
            .where(Conversation.class)
            .greaterThan("numberOfUnread", 0)
            .findFirst();
        final boolean areUnreadMessages = result != null;
        realm.close();
        return areUnreadMessages;
    }

    public Single<SofaMessage> getSofaMessageById(final String id) {
        return Single.fromCallable(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            final SofaMessage result =
                realm
                    .where(SofaMessage.class)
                    .equalTo("privateKey", id)
                    .findFirst();
            final SofaMessage sofaMessage = realm.copyFromRealm(result);
            realm.close();
            return sofaMessage;
        })
        .subscribeOn(Schedulers.from(dbThread));
    }


    //##############################################################################################
    // Deletion
    //##############################################################################################

    public Completable deleteByThreadId(final String threadId) {
        return Completable.fromAction(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            final Conversation conversationToDelete = realm
                    .where(Conversation.class)
                    .equalTo(THREAD_ID_FIELD, threadId)
                    .findFirst();
            if (conversationToDelete != null) conversationToDelete.cascadeDelete();
            realm.commitTransaction();
            realm.close();
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    public Completable deleteMessageById(final Recipient receiver, final SofaMessage message) {
        return Completable.fromAction(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            realm
                    .where(SofaMessage.class)
                    .equalTo(MESSAGE_ID_FIELD, message.getPrivateKey())
                    .findFirst()
                    .deleteFromRealm();
            realm.commitTransaction();
            realm.close();
        })
        .observeOn(Schedulers.immediate())
        .subscribeOn(Schedulers.from(dbThread))
        .andThen(updateLatestMessage(receiver.getThreadId()))
        .doOnCompleted(() -> broadcastDeletedChatMessage(receiver.getThreadId(), message))
        .doOnError(this::handleError);
    }

    //##############################################################################################
    // Set Conversation State
    //##############################################################################################

    public Single<Conversation> muteConversation(final Conversation conversation, final boolean mute) {
        return Single.fromCallable(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            conversation.getConversationStatus().setMuted(mute);
            realm.copyToRealmOrUpdate(conversation);
            realm.commitTransaction();
            realm.close();
            return conversation;
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    public Single<Conversation> acceptConversation(final Conversation conversation) {
        return Single.fromCallable(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            conversation.getConversationStatus().setAccepted(true);
            realm.copyToRealmOrUpdate(conversation);
            realm.commitTransaction();
            realm.close();
            return conversation;
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    public void resetUnreadMessageCounter(final String threadId) {
        Single.fromCallable(() -> {
            final Conversation storedConversation = loadWhere(THREAD_ID_FIELD, threadId);
            if (storedConversation == null) {
                return null;
            }

            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            storedConversation.resetUnreadCounter();
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

    //##############################################################################################
    // Broadcasting changes
    //##############################################################################################

    private void broadcastNewChatMessage(final String threadId, final SofaMessage newMessage) {
        if (watchedThreadId == null || !watchedThreadId.equals(threadId)) {
            return;
        }
        NEW_MESSAGE_SUBJECT.onNext(newMessage);
    }

    private void broadcastUpdatedChatMessage(final String threadId, final SofaMessage updatedMessage) {
        if (watchedThreadId == null || !watchedThreadId.equals(threadId)) {
            return;
        }
        UPDATED_MESSAGE_SUBJECT.onNext(updatedMessage);
    }

    private void broadcastDeletedChatMessage(final String threadId, final SofaMessage deletedMessage) {
        if (watchedThreadId == null || !watchedThreadId.equals(threadId)) {
            return;
        }
        DELETED_MESSAGE_SUBJECT.onNext(deletedMessage);
    }

    private void broadcastConversation(final Conversation conversation) {
        broadcastConversationChanged(conversation);
        broadcastConversationUpdated(conversation);
    }

    private void broadcastConversationChanged(final Conversation conversation) {
        CONVERSATION_CHANGED_SUBJECT.onNext(conversation);
    }

    private void broadcastConversationUpdated(final Conversation conversation) {
        if (watchedThreadId == null || !watchedThreadId.equals(conversation.getThreadId())) {
            return;
        }
        CONVERSATION_UPDATED_SUBJECT.onNext(conversation);
    }

    private void handleError(final Throwable throwable) {
        LogUtil.exception(getClass(), throwable);
    }
}
