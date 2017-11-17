package com.toshi.model.local;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ConversationStatus extends RealmObject {
    @PrimaryKey
    private String threadId;
    private boolean isMuted;
    private boolean isAccepted;

    public ConversationStatus() {}

    public ConversationStatus(final String threadId) {
        this.threadId = threadId;
    }

    public String getThreadId() {
        return this.threadId;
    }

    public boolean isMuted() {
        return this.isMuted;
    }

    public ConversationStatus setMuted(final boolean muted) {
        this.isMuted = muted;
        return this;
    }

    public boolean isAccepted() {
        return this.isAccepted;
    }

    public ConversationStatus setAccepted(final boolean accepted) {
        this.isAccepted = accepted;
        return this;
    }
}
