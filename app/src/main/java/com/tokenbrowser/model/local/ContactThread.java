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

package com.tokenbrowser.model.local;


import android.support.annotation.Nullable;

import com.tokenbrowser.model.sofa.SofaMessage;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ContactThread extends RealmObject {

    @PrimaryKey
    private String threadId;
    private Recipient recipient;
    private SofaMessage latestMessage;
    private long updatedTime;
    private RealmList<SofaMessage> allMessages;
    private int numberOfUnread;

    public ContactThread() {}

    public ContactThread(final User user) {
        this.recipient = new Recipient(user);
        this.threadId = user.getTokenId();
    }

    public ContactThread(final Group group) {
        this.recipient = new Recipient(group);
        this.threadId = group.getId();
    }

    public String getThreadId() {
        return threadId;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public SofaMessage getLatestMessage() {
        return latestMessage;
    }

    public ContactThread setLatestMessage(final SofaMessage latestMessage) {
        if (isDuplicateMessage(latestMessage)) {
            return this;
        }
        this.latestMessage = latestMessage;
        this.updatedTime = latestMessage.getCreationTime();
        addMessage(latestMessage);
        return this;
    }

    private boolean isDuplicateMessage(final SofaMessage message) {
        return this.allMessages != null && this.allMessages.contains(message);
    }

    public void addMessage(final SofaMessage latestMessage) {
        if (this.allMessages == null) {
            this.allMessages = new RealmList<>();
        }

        this.allMessages.add(latestMessage);
    }

    public List<SofaMessage> getAllMessages() {
        return allMessages;
    }

    public int getNumberOfUnread() {
        return numberOfUnread;
    }

    public void setNumberOfUnread(final int numberOfUnread) {
        this.numberOfUnread = numberOfUnread;
    }

    // Helper functions
    public final boolean isGroup() {
        return this.recipient.isGroup();
    }

    @Nullable
    public final User getUserRecipient() {
        return this.recipient.getUser();
    }

    @Nullable
    public final Group getGroupRecipient() {
        return this.recipient.getGroup();
    }

    @Override
    public int hashCode() {
        return threadId.hashCode();
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ContactThread))return false;
        final ContactThread otherContactThreadMessage = (ContactThread) other;
        return otherContactThreadMessage.getThreadId().equals(this.threadId);
    }
}
