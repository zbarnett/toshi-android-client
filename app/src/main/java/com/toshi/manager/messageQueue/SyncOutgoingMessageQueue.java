package com.toshi.manager.messageQueue;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

public class SyncOutgoingMessageQueue extends OutgoingMessageQueue {
    @Override
    /* package */ Scheduler getSubscribeThread() {
        return AndroidSchedulers.mainThread();
    }

    @Override
    /* package */ Scheduler getObserveThread() {
        return AndroidSchedulers.mainThread();
    }
}
