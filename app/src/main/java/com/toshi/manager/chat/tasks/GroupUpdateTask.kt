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

import com.toshi.manager.store.ConversationStore
import com.toshi.model.local.Group
import com.toshi.model.local.IncomingMessage
import com.toshi.util.LogUtil
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage

class GroupUpdateTask(
        private val messageReceiver: SignalServiceMessageReceiver,
        private val conversationStore: ConversationStore
) {

    fun run(dataMessage: SignalServiceDataMessage): IncomingMessage? {
        val signalGroup = dataMessage.groupInfo.get()
        Group()
            .updateFromSignalGroup(signalGroup, messageReceiver)
            .subscribe(
                    { this.conversationStore.saveGroup(it) },
                    { LogUtil.e(javaClass, "Error creating incoming group. $it") }
            )
        return null
    }
}