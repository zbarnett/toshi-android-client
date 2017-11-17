package com.toshi.presenter.chat;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.RemoteInput;

import com.toshi.model.local.Recipient;
import com.toshi.model.local.User;
import com.toshi.model.sofa.Message;
import com.toshi.model.sofa.SofaAdapters;
import com.toshi.model.sofa.SofaMessage;
import com.toshi.manager.messageQueue.SyncOutgoingMessageQueue;
import com.toshi.util.LogUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.notification.ChatNotificationManager;

import static com.toshi.view.notification.ChatNotificationManager.KEY_TEXT_REPLY;


public class DirectReplyService extends IntentService {

    public static final int REQUEST_CODE = 0;
    public static final String TOSHI_ID = "toshiId";

    private SyncOutgoingMessageQueue outgoingMessageQueue;

    public DirectReplyService() {
        super("DirectReplyService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        final String toshiId = intent.getStringExtra((TOSHI_ID));
        final String userInput = getUserInputFromIntent(intent);

        tryInit(toshiId, userInput);
    }

    private String getUserInputFromIntent(final Intent intent) {
        final Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        final CharSequence replyText = remoteInput.getCharSequence(KEY_TEXT_REPLY);
        return replyText != null ? replyText.toString() : null;
    }

    private void tryInit(final String toshiId, final String userInput) {
        try {
            tryInit();

            final Recipient recipient = getRecipient(toshiId);
            final User localUser = getLocalUser();
            sendMessage(recipient, localUser, userInput);
        } catch (Exception e) {
            LogUtil.e(getClass(), "Error trying to initiate app " + e);
        }
    }

    private void tryInit() throws Exception {
        BaseApplication
                .get()
                .getToshiManager()
                .tryInit()
                .await();
    }

    private Recipient getRecipient(final String recipientId) throws Exception {
        return BaseApplication
                .get()
                .getRecipientManager()
                .getFromId(recipientId)
                .toBlocking()
                .value();
    }

    private User getLocalUser() throws Exception {
        return BaseApplication
                .get()
                .getUserManager()
                .getCurrentUser()
                .toBlocking()
                .value();
    }

    private void sendMessage(final Recipient receiver, final User localUser, final String userInput) {
        initOutgoingMessageQueue(receiver);

        final Message message = new Message().setBody(userInput);
        final String messageBody = SofaAdapters.get().toJson(message);
        final SofaMessage sofaMessage = new SofaMessage().makeNew(localUser, messageBody);

        this.outgoingMessageQueue.send(sofaMessage);
        this.outgoingMessageQueue.clear();
        ChatNotificationManager.showChatNotification(receiver, sofaMessage);
    }

    private void initOutgoingMessageQueue(final Recipient recipient) {
        if (this.outgoingMessageQueue != null) return;
        this.outgoingMessageQueue = new SyncOutgoingMessageQueue();
        this.outgoingMessageQueue.init(recipient);
    }
}
