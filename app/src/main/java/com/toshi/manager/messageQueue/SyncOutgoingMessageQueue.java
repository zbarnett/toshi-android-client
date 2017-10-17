package com.toshi.manager.messageQueue;

public class SyncOutgoingMessageQueue extends OutgoingMessageQueue {
    public SyncOutgoingMessageQueue() {
        super(false);
    }
}
