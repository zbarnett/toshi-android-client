package com.toshi.model.local;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MutedConversation extends RealmObject {
    @PrimaryKey
    private String threadId;

    public MutedConversation() {}

    public MutedConversation(final String threadId) {
        this.threadId = threadId;
    }
}
