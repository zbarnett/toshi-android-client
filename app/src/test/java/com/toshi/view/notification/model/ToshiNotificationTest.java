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

package com.toshi.view.notification.model;


import com.toshi.model.sofa.Message;
import com.toshi.model.sofa.SofaAdapters;
import com.toshi.model.sofa.SofaMessage;
import com.toshi.model.sofa.SofaType;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

public class ToshiNotificationTest {

    private final String expectedNotificationId = "notificationId";
    private final String expectedMessageId = "messageId";
    private final String unacceptedMessage = "Unaccepted message";
    private SofaMessage unreadMessage;
    private ToshiNotification notification;

    @Before
    public void setup() {
        this.unreadMessage = createUnreadMessage(this.expectedMessageId);
        this.notification = new TestToshiNotification(expectedNotificationId)
                .setIsAccepted(true);
    }

    @Test
    public void instantiateWithIdReturnedCorrectly() {
        assertThat(this.notification.getId(), is(expectedNotificationId));
    }

    @Test
    public void getTagIsAsExpected() {
        assertThat(this.notification.getTag(), is(ToshiNotification.DEFAULT_TAG));
    }

    @Test
    public void isConstructedWithNoUnreadMessages() {
        assertThat(this.notification.getNumberOfUnreadMessages(), is(0));
    }

    @Test
    public void getLastFewMessagesAfterConstructionIsEmptyList() {
        assertThat(this.notification.getLastFewMessages().size(), is(0));
    }

    @Test
    public void getLastMessageWhenNoMessagesReturnsNull() {
        assertNull(this.notification.getLastMessage());
    }

    @Test
    public void getTypeOfLastMessageWhenNoMessagesReturnsUnknown() {
        assertThat(this.notification.getTypeOfLastMessage(), is(SofaType.UNKNOWN));
    }

    @Test
    public void addUnreadMessageIncreasesNumberOfUnreadMessages() {
        this.notification.addUnreadMessage(this.unreadMessage);
        assertThat(this.notification.getNumberOfUnreadMessages(), is(1));
    }

    @Test
    public void addSameMessageMultipleTimesIncreasesNumberOfUnreadMessages() {
        final int numExpected = 5;
        for (int i = 0; i < numExpected; i++) {
            this.notification.addUnreadMessage(this.unreadMessage);
        }
        assertThat(this.notification.getNumberOfUnreadMessages(), is(numExpected));
    }

    @Test
    public void getLastFewMessagesAfterAddingSingleMessageReturnsListOfSize1() {
        this.notification.addUnreadMessage(this.unreadMessage);
        assertThat(this.notification.getLastFewMessages().size(), is(1));
    }

    @Test
    public void getLastFewMessagesAfterAddingSingleMessageReturnsListContainingTheSingleMessage() {
        this.notification.addUnreadMessage(this.unreadMessage);
        assertThat(this.notification.getLastFewMessages().get(0), is(this.expectedMessageId));
    }

    @Test
    public void unreadMessagesAreAddedToEndOfLastFewMessages() {
        final SofaMessage unreadMessage0 = createUnreadMessage("id: 0");
        final SofaMessage unreadMessage1 = createUnreadMessage("id: 1");
        this.notification.addUnreadMessage(unreadMessage0);
        this.notification.addUnreadMessage(unreadMessage1);
        assertThat(this.notification.getLastFewMessages().get(0), is("id: 0"));
        assertThat(this.notification.getLastFewMessages().get(1), is("id: 1"));
    }

    @Test
    public void getLastFewMessagesAfterAddingSingleMessageReturnsThatMessage() {
        this.notification.addUnreadMessage(this.unreadMessage);
        assertThat(this.notification.getLastMessage(), is(this.expectedMessageId));
    }

    @Test
    public void getLastFewMessagesAfterAddingMultiplesMessageReturnsLastAddedMessage() {
        final SofaMessage unreadMessage0 = createUnreadMessage("id: 0");
        final SofaMessage unreadMessage1 = createUnreadMessage("id: 1");
        this.notification.addUnreadMessage(unreadMessage0);
        this.notification.addUnreadMessage(unreadMessage1);
        assertThat(this.notification.getLastMessage(), is("id: 1"));
    }

    @Test
    public void addingMoreThan5UnreadMessagesReturnsTrueCountOfUnread() {
        addMultipleUnreadMessages(10);
        assertThat(this.notification.getNumberOfUnreadMessages(), is(10));
    }

    @Test
    public void addingMoreThan5UnreadMessagesReturns5MessagesInLastFewMessages() {
        addMultipleUnreadMessages(20);
        assertThat(this.notification.getLastFewMessages().size(), is(5));
    }

    @Test
    public void addingMoreThan5UnreadMessagesReturnsLastAddedMessagesInLastFewMessages() {
        addMultipleUnreadMessages(10);
        assertThat(this.notification.getLastFewMessages().get(4), is("9"));
    }

    @Test
    public void canCallGetLastFewMessagesWhilstAddingUnreadMessagesInASeparateThread() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> addMultipleUnreadMessages(1000)).start();
            Thread.sleep(1);
            this.notification.getLastFewMessages();
        }
    }

    @Test
    public void isAllMessagesOfUnacceptedTypeWhenUnaccepted() {
        this.notification.setIsAccepted(false);
        final SofaMessage unreadMessage0 = createUnreadMessage("id: 0");
        final SofaMessage unreadMessage1 = createUnreadMessage("id: 1");
        this.notification.addUnreadMessage(unreadMessage0);
        this.notification.addUnreadMessage(unreadMessage1);
        assertThat(this.notification.getLastFewMessages().get(0), is("Unaccepted message"));
        assertThat(this.notification.getLastFewMessages().get(1), is("Unaccepted message"));
    }

    private void addMultipleUnreadMessages(final int numberToCreate) {
        for (int i = 0; i < numberToCreate; i++) {
            final SofaMessage unreadMessage = createUnreadMessage(String.valueOf(i));
            this.notification.addUnreadMessage(unreadMessage);
        }
    }

    private SofaMessage createUnreadMessage(final String id) {
        final Message message = new Message().setBody(id);
        final String sofaPayload = SofaAdapters.get().toJson(message);
        return new SofaMessage().makeNew(sofaPayload);
    }

    class TestToshiNotification extends ToshiNotification {
        TestToshiNotification(final String id) {
            super(id);
        }

        @Override
        public String getTitle() {
            return "TestToshiNotification";
        }

        @Override
        String getUnacceptedText() {
            return unacceptedMessage;
        }
    }
}
