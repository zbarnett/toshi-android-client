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

package com.toshi.presenter.chat

import com.toshi.manager.messageQueue.AsyncOutgoingMessageQueue
import com.toshi.model.local.Recipient
import com.toshi.model.local.User
import com.toshi.model.sofa.Command
import com.toshi.model.sofa.Control
import com.toshi.model.sofa.Message
import com.toshi.model.sofa.PaymentRequest
import com.toshi.model.sofa.SofaAdapters
import com.toshi.model.sofa.SofaMessage

class ChatMessageQueue(private val outgoingMessageQueue: AsyncOutgoingMessageQueue) {

    fun init(recipient: Recipient) = outgoingMessageQueue.init(recipient)

    fun addPaymentRequestToQueue(paymentRequest: PaymentRequest, localUser: User) {
        val messageBody = SofaAdapters.get().toJson(paymentRequest)
        val message = SofaMessage().makeNew(localUser, messageBody)
        outgoingMessageQueue.send(message)
    }

    fun addMessageToQueue(userInput: String, localUser: User) {
        val message = Message().setBody(userInput)
        val messageBody = SofaAdapters.get().toJson(message)
        val sofaMessage = SofaMessage().makeNew(localUser, messageBody)
        outgoingMessageQueue.send(sofaMessage)
    }

    fun addCommandMessageToQueue(control: Control, localUser: User) {
        val command = Command()
                .setBody(control.label)
                .setValue(control.value)
        val commandPayload = SofaAdapters.get().toJson(command)
        val sofaMessage = SofaMessage().makeNew(localUser, commandPayload)
        outgoingMessageQueue.send(sofaMessage)
    }

    fun addMediaMessageToQueue(filePath: String, localUser: User) {
        val messageBody = SofaAdapters.get().toJson(Message())
        val sofaMessage = SofaMessage()
                .makeNew(localUser, messageBody)
                .setAttachmentFilePath(filePath)
        outgoingMessageQueue.send(sofaMessage)
    }

    fun updateRecipient(recipient: Recipient) = outgoingMessageQueue.updateRecipient(recipient)

    fun clear() = outgoingMessageQueue.clear()
}