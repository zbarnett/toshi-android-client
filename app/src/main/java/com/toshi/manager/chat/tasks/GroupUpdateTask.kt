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

import com.toshi.manager.chat.SofaMessageSender
import com.toshi.manager.store.ConversationStore
import com.toshi.model.local.Group
import com.toshi.model.local.IncomingMessage
import com.toshi.util.LogUtil
import com.toshi.view.BaseApplication
import org.spongycastle.util.encoders.Hex
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage
import org.whispersystems.signalservice.api.messages.SignalServiceGroup
import rx.schedulers.Schedulers

class GroupUpdateTask(
        private val messageReceiver: SignalServiceMessageReceiver,
        private val messageSender: SofaMessageSender,
        private val conversationStore: ConversationStore
) {

    fun run(messageSource: String, dataMessage: SignalServiceDataMessage): IncomingMessage? {
        val signalGroup = dataMessage.groupInfo.get()
        when (signalGroup.type) {
            SignalServiceGroup.Type.REQUEST_INFO -> handleRequestGroupInfo(messageSource, dataMessage)
            SignalServiceGroup.Type.QUIT -> handleLeaveGroup(messageSource, dataMessage)
            else -> handleGroupUpdate(signalGroup)
        }
        return null
    }

    private fun handleRequestGroupInfo(messageSource: String, dataMessage: SignalServiceDataMessage) {
        if (!dataMessage.groupInfo.isPresent) return
        val signalGroup = dataMessage.groupInfo.get()
        Group
                .fromSignalGroup(signalGroup)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        { group -> messageSender.sendGroupInfo(messageSource, group) },
                        { LogUtil.e(javaClass, "Request for group info failed.") }
                )
    }

    private fun handleLeaveGroup(messageSource: String, dataMessage: SignalServiceDataMessage) {
        if (!dataMessage.groupInfo.isPresent) return
        val signalGroup = dataMessage.groupInfo.get()
        BaseApplication.get()
                .recipientManager
                .getUserFromToshiId(messageSource)
                .subscribe(
                        { conversationStore.removeUserFromGroup(it, Hex.toHexString(signalGroup.groupId)) },
                        { LogUtil.e(javaClass, "Error handling leave group. $it") }
                )
    }

    private fun handleGroupUpdate(signalGroup: SignalServiceGroup?) {
        Group()
                .updateFromSignalGroup(signalGroup, messageReceiver)
                .subscribe(
                        { this.conversationStore.saveGroup(it) },
                        { LogUtil.e(javaClass, "Error creating incoming group. $it") }
                )
    }
}