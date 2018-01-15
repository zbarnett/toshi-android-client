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
import org.whispersystems.signalservice.api.messages.SignalServiceGroup
import rx.Single

class GroupCreatedTask(private val conversationStore: ConversationStore) {

    fun run(signalGroup: SignalServiceGroup): Single<Boolean> {
        return Group.fromSignalGroup(signalGroup)
                .map { it == null }
                .flatMap { isNewGroup -> saveGroupAndAddStatusMessage(isNewGroup, signalGroup) }
    }

    private fun saveGroupAndAddStatusMessage(isNewGroup: Boolean, signalGroup: SignalServiceGroup): Single<Boolean> {
        if (!isNewGroup) return Single.just(isNewGroup)
        return conversationStore.saveSignalGroup(signalGroup)
                .flatMapCompletable { conversationStore.addAddedToGroupStatusMessage(it).toCompletable() }
                .toSingle { isNewGroup }
    }
}