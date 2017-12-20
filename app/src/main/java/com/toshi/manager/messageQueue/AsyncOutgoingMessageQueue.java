package com.toshi.manager.messageQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Scheduler;
import rx.schedulers.Schedulers;

public class AsyncOutgoingMessageQueue extends OutgoingMessageQueue {

    private final ExecutorService messageThread;

    public AsyncOutgoingMessageQueue() {
        this.messageThread = Executors.newSingleThreadExecutor();
    }

    @Override
    /* package */ Scheduler getSubscribeThread() {
        return Schedulers.from(messageThread);
    }

    @Override
    /* package */ Scheduler getObserveThread() {
        return Schedulers.from(messageThread);
    }
}
