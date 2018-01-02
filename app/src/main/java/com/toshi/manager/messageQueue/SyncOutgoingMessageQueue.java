package com.toshi.manager.messageQueue;

import rx.Scheduler;
import rx.schedulers.Schedulers;

public class SyncOutgoingMessageQueue extends OutgoingMessageQueue {
    @Override
    /* package */ Scheduler getSubscribeThread() {
        return Schedulers.immediate();
    }

    @Override
    /* package */ Scheduler getObserveThread() {
        return Schedulers.immediate();
    }
}
