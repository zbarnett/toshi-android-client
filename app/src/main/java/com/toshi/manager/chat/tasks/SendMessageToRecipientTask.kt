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

package com.toshi.manager.chat.tasks

import com.toshi.crypto.signal.store.ProtocolStore
import com.toshi.manager.model.SofaMessageTask
import com.toshi.manager.store.ConversationStore
import com.toshi.manager.store.PendingMessageStore
import com.toshi.model.local.SendState
import com.toshi.model.network.SofaError
import com.toshi.model.sofa.OutgoingAttachment
import com.toshi.util.FileUtil.buildSignalServiceAttachment
import com.toshi.util.LogUtil
import com.toshi.view.BaseApplication
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.util.Hex
import org.whispersystems.signalservice.api.SignalServiceMessageSender
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage
import org.whispersystems.signalservice.api.messages.SignalServiceGroup
import org.whispersystems.signalservice.api.push.SignalServiceAddress
import org.whispersystems.signalservice.api.push.exceptions.EncapsulatedExceptions
import org.whispersystems.signalservice.api.push.exceptions.UnregisteredUserException
import java.io.FileNotFoundException
import java.io.IOException

class SendMessageToRecipientTask(
        private val conversationStore: ConversationStore,
        private val pendingMessageStore: PendingMessageStore,
        private val protocolStore: ProtocolStore,
        private val signalMessageSender: SignalServiceMessageSender
) {
    fun run(messageTask: SofaMessageTask, saveMessageToDatabase: Boolean) {
        if (messageTask.receiver.isGroup) sendMessageToGroup(messageTask, saveMessageToDatabase)
        else sendMessageToUser(messageTask, saveMessageToDatabase)
    }

    private fun sendMessageToGroup(messageTask: SofaMessageTask, saveMessageToDatabase: Boolean) {
        if (saveMessageToDatabase) saveMessageToDatabase(messageTask)
        if (isOfflineAndCache(messageTask, saveMessageToDatabase)) return

        try {
            sendToSignal(messageTask.receiver.group.memberAddresses, messageTask)
            if (saveMessageToDatabase) {
                messageTask.sofaMessage.sendState = SendState.STATE_SENT
                updateExistingMessage(messageTask)
            }
        } catch (e: EncapsulatedExceptions) {
            for (uie in e.untrustedIdentityExceptions) {
                LogUtil.error(javaClass, "Keys have changed.")
                protocolStore.saveIdentity(SignalProtocolAddress(uie.e164Number, SignalServiceAddress.DEFAULT_DEVICE_ID), uie.identityKey)
            }
        } catch (ex: IOException) {
            LogUtil.error(javaClass, ex.toString())
            val errorMessage = getErrorMessageFromException(ex)
            if (saveMessageToDatabase) saveAndUpdateExistingMessageWithErrorMessage(messageTask, errorMessage)
        }
    }

    private fun sendMessageToUser(messageTask: SofaMessageTask, saveMessageToDatabase: Boolean) {
        if (saveMessageToDatabase) saveMessageToDatabase(messageTask)
        if (isOfflineAndCache(messageTask, saveMessageToDatabase)) return

        try {
            sendToSignal(messageTask.receiver.user.toshiId, messageTask)
            if (saveMessageToDatabase) {
                messageTask.sofaMessage.sendState = SendState.STATE_SENT
                updateExistingMessage(messageTask)
            }
        } catch (ue: UntrustedIdentityException) {
            LogUtil.error(javaClass, "Keys have changed. " + ue)
            protocolStore.saveIdentity(SignalProtocolAddress(ue.e164Number, SignalServiceAddress.DEFAULT_DEVICE_ID), ue.identityKey)
        } catch (ex: IOException) {
            LogUtil.error(javaClass, ex.toString())
            val errorMessage = getErrorMessageFromException(ex)
            if (saveMessageToDatabase) saveAndUpdateExistingMessageWithErrorMessage(messageTask, errorMessage)
        }
    }

    private fun getErrorMessageFromException(exception: Exception): SofaError {
        return when (exception) {
            is UnregisteredUserException -> SofaError().createUserUnavailableMessage(BaseApplication.get())
            else -> SofaError().createNotDeliveredMessage(BaseApplication.get())
        }
    }

    private fun saveAndUpdateExistingMessageWithErrorMessage(messageTask: SofaMessageTask, errorMessage: SofaError) {
        try {
            messageTask.sofaMessage.errorMessage = errorMessage
            messageTask.sofaMessage.sendState = SendState.STATE_FAILED
            updateExistingMessage(messageTask)
            savePendingMessage(messageTask)
        } catch (ex: IOException) {
            LogUtil.error(javaClass, ex.toString())
        }
    }

    private fun isOfflineAndCache(messageTask: SofaMessageTask, saveMessageToDatabase: Boolean): Boolean {
        val isConnected = BaseApplication.get().isConnected
        if (!isConnected && saveMessageToDatabase) {
            messageTask.sofaMessage.sendState = SendState.STATE_FAILED
            updateExistingMessage(messageTask)
            savePendingMessage(messageTask)
        }
        return !isConnected
    }

    @Throws(IOException::class, EncapsulatedExceptions::class)
    private fun sendToSignal(signalAddresses: List<SignalServiceAddress>, messageTask: SofaMessageTask) {
        val message = buildMessage(messageTask)
        signalMessageSender.sendMessage(signalAddresses, message)
    }

    @Throws(UntrustedIdentityException::class, IOException::class)
    private fun sendToSignal(signalAddress: String, messageTask: SofaMessageTask) {
        val receivingAddress = SignalServiceAddress(signalAddress)
        val message = buildMessage(messageTask)
        signalMessageSender.sendMessage(receivingAddress, message)
    }

    @Throws(FileNotFoundException::class)
    private fun buildMessage(messageTask: SofaMessageTask): SignalServiceDataMessage {
        val messageBuilder = SignalServiceDataMessage
                .newBuilder()
                .withBody((messageTask.sofaMessage.asSofaMessage))
        tryAddAttachment(messageTask, messageBuilder)
        tryAddGroup(messageTask, messageBuilder)
        return messageBuilder.build()
    }

    private fun tryAddGroup(messageTask: SofaMessageTask, messageBuilder: SignalServiceDataMessage.Builder) {
        try {
            if (!messageTask.receiver.isGroup) return
            val signalGroup = SignalServiceGroup(Hex.fromStringCondensed(messageTask.receiver.group.id))
            messageBuilder.asGroupMessage(signalGroup)
        } catch (ex: Exception) {
            LogUtil.i(javaClass, "Tried and failed to attach group. $ex")
        }
    }

    private fun tryAddAttachment(messageTask: SofaMessageTask, messageBuilder: SignalServiceDataMessage.Builder) {
        try {
            val outgoingAttachment = OutgoingAttachment(messageTask.sofaMessage)
            if (outgoingAttachment.isValid) {
                val signalAttachment = buildSignalServiceAttachment(outgoingAttachment)
                messageBuilder.withAttachment(signalAttachment)
            }
        } catch (ex: Exception) {
            when (ex) {
                is FileNotFoundException, is IllegalStateException -> LogUtil.i(javaClass, "Tried and failed to attach attachment. $ex")
                else -> throw ex
            }
        }
    }

    private fun saveMessageToDatabase(messageTask: SofaMessageTask) = conversationStore.saveNewMessage(messageTask.receiver, messageTask.sofaMessage)
    private fun savePendingMessage(messageTask: SofaMessageTask) = pendingMessageStore.save(messageTask.receiver, messageTask.sofaMessage)
    private fun updateExistingMessage(messageTask: SofaMessageTask) = conversationStore.updateMessage(messageTask.receiver, messageTask.sofaMessage)
}
