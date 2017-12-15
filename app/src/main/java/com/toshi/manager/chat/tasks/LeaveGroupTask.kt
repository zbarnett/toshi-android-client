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

import com.toshi.model.local.Group
import org.whispersystems.signalservice.api.SignalServiceMessageSender
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage
import org.whispersystems.signalservice.api.messages.SignalServiceGroup
import rx.Completable

class LeaveGroupTask(
        private val signalMessageSender: SignalServiceMessageSender
) : BaseGroupTask() {
    fun run(group: Group): Completable {
        return Completable.fromAction {
            val signalGroup = SignalServiceGroup
                    .newBuilder(SignalServiceGroup.Type.QUIT)
                    .withId(group.idBytes)
                    .build()
            val dataMessage = SignalServiceDataMessage
                    .newBuilder()
                    .asGroupMessage(signalGroup)
                    .withTimestamp(System.currentTimeMillis())
                    .build()
            signalMessageSender.sendMessage(group.memberAddresses, dataMessage)
        }
        .onErrorResumeNext { handleException(it) }
    }
}