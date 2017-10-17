package com.toshi.manager.messageQueue;

public class AsyncOutgoingMessageQueue extends OutgoingMessageQueue {
    public AsyncOutgoingMessageQueue() {
        super(true);
    }
}
