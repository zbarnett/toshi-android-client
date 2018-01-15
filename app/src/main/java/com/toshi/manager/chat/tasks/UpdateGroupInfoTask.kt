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
import org.spongycastle.util.encoders.Hex
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver
import org.whispersystems.signalservice.api.messages.SignalServiceGroup
import rx.Completable

class UpdateGroupInfoTask(
        private val conversationStore: ConversationStore,
        private val messageReceiver: SignalServiceMessageReceiver
) {

    fun run(messageSource: String, signalGroup: SignalServiceGroup) {
        val isNewGroup = GroupCreatedTask(conversationStore).run(signalGroup).toBlocking().value()
        updateGroupName(messageSource, signalGroup, !isNewGroup).await()
        updateParticipants(messageSource, signalGroup, !isNewGroup).await()
        updateAvatar(signalGroup).await()
    }

    private fun updateGroupName(messageSource: String, signalGroup: SignalServiceGroup, addStatusMessage: Boolean): Completable {
        if (signalGroup.groupId == null || !signalGroup.name.isPresent) return Completable.error(Throwable("Signal groupId/name is null"))
        val groupId = Hex.toHexString(signalGroup.groupId)
        return NewGroupNameTask(conversationStore, addStatusMessage)
                .run(messageSource, groupId, signalGroup.name.get())
                .onErrorComplete()
    }

    private fun updateParticipants(messageSource: String, signalGroup: SignalServiceGroup, addStatusMessage: Boolean): Completable {
        if (signalGroup.groupId == null) return Completable.error(Throwable("Signal group id is null"))
        val groupId = Hex.toHexString(signalGroup.groupId)
        val signalGroupIds = signalGroup.members?.get() ?: emptyList()
        return NewGroupMembersTask(conversationStore, addStatusMessage)
                .run(groupId, messageSource, signalGroupIds)
                .onErrorComplete()
    }

    private fun updateAvatar(signalGroup: SignalServiceGroup): Completable {
        if (signalGroup.groupId == null) return Completable.error(Throwable("Signal group id is null"))
        val groupId = Hex.toHexString(signalGroup.groupId)
        return NewGroupAvatarTask(conversationStore, messageReceiver)
                .run(groupId, signalGroup)
                .onErrorComplete()
    }
}