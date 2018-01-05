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
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver
import org.whispersystems.signalservice.api.messages.SignalServiceGroup
import rx.Completable

class NewGroupAvatarTask(
        private val conversationStore: ConversationStore,
        private val messageReceiver: SignalServiceMessageReceiver
) {

    fun run(groupId: String, signalGroup: SignalServiceGroup): Completable {
        return Group.fromId(groupId)
                .flatMapCompletable { processAvatarAndUpdateGroup(it, signalGroup) }
    }

    private fun processAvatarAndUpdateGroup(group: Group?, signalGroup: SignalServiceGroup): Completable {
        return group?.let {
            it.processAvatar(signalGroup, messageReceiver)
                    .andThen(conversationStore.saveGroup(it))
        } ?: Completable.error(Throwable("Group is null"))
    }
}