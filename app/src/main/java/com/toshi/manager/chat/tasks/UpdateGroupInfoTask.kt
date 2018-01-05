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
import com.toshi.util.LogUtil
import org.spongycastle.util.encoders.Hex
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver
import org.whispersystems.signalservice.api.messages.SignalServiceGroup
import rx.Completable
import rx.Single

class UpdateGroupInfoTask(
        private val conversationStore: ConversationStore,
        private val messageReceiver: SignalServiceMessageReceiver
) {

    fun run(messageSource: String, signalGroup: SignalServiceGroup) {
        updateGroupName(messageSource, signalGroup)
                .andThen(updateParticipants(messageSource, signalGroup))
                .flatMapCompletable { conversationStore.saveGroup(it) }
                .subscribe(
                        {},
                        { LogUtil.e(javaClass, "Error creating incoming group. $it") }
                )
    }

    private fun updateGroupName(messageSource: String, signalGroup: SignalServiceGroup): Completable {
        if (signalGroup.groupId == null || signalGroup.name?.get() == null) return Completable.complete()
        val groupId = Hex.toHexString(signalGroup.groupId)
        return NewGroupNameTask(conversationStore)
                .run(messageSource, groupId, signalGroup.name.get())
                .onErrorComplete()
    }

    private fun updateParticipants(messageSource: String, signalGroup: SignalServiceGroup): Single<Group> {
        val groupId = Hex.toHexString(signalGroup.groupId)
        val signalGroupIds = signalGroup.members?.get() ?: emptyList()
        return NewGroupParticipantsTask(conversationStore)
                .run(groupId, messageSource, signalGroupIds)
                .onErrorComplete()
                .andThen(Group().updateFromSignalGroup(signalGroup, messageReceiver))
    }
}