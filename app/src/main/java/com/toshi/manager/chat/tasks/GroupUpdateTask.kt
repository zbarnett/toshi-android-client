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
            SignalServiceGroup.Type.QUIT -> handleLeaveGroup(messageSource, signalGroup)
            else -> handleGroupUpdate(messageSource, signalGroup)
        }
        return null
    }

    private fun handleRequestGroupInfo(messageSource: String, dataMessage: SignalServiceDataMessage) {
        if (!dataMessage.groupInfo.isPresent) return
        val signalGroup = dataMessage.groupInfo.get()
        Group
                .fromSignalGroup(signalGroup)
                .flatMapCompletable { messageSender.sendGroupInfo(messageSource, it) }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        {},
                        { LogUtil.e(javaClass, "Request for group info failed.") }
                )
    }

    private fun handleLeaveGroup(messageSource: String, signalGroup: SignalServiceGroup) {
        LeftGroupTask(conversationStore).run(messageSource, signalGroup)
    }

    private fun handleGroupUpdate(messageSource: String, signalGroup: SignalServiceGroup) {
        UpdateGroupInfoTask(conversationStore, messageReceiver).run(messageSource, signalGroup)
    }
}