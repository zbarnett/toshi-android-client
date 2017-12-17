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
import com.toshi.model.local.User
import com.toshi.view.BaseApplication
import rx.Completable
import rx.Observable
import rx.Single

class NewGroupParticipantsTask(private val conversationStore: ConversationStore) {

    private val recipientManager by lazy { BaseApplication.get().recipientManager }

    fun run(groupId: String, senderId: String, participantsIds: List<String>): Completable {
        return recipientManager
                .getGroupFromId(groupId)
                .map { findNewGroupParticipants(it, participantsIds) }
                .flatMapCompletable { addNewGroupParticipantsStatusMessage(groupId, senderId, it) }
    }

    private fun addNewGroupParticipantsStatusMessage(groupId: String, senderId: String, newParticipantsIds: List<String>): Completable {
        if (newParticipantsIds.isEmpty()) return Completable.complete()
        return Single.zip(
                getUserFromId(senderId),
                getNewParticipants(newParticipantsIds),
                { user, newParticipants -> Pair(user, newParticipants) }
        )
        .flatMap { conversationStore.addNewGroupParticipantsStatusMessage(groupId, it.first, it.second) }
        .toCompletable()
    }

    private fun getNewParticipants(newUsers: List<String>): Single<List<User>> {
        return Observable.from(newUsers)
                .flatMap { getUserFromId(it).toObservable() }
                .toList()
                .toSingle()
    }

    private fun getUserFromId(id: String) = recipientManager.getUserFromToshiId(id)

    private fun findNewGroupParticipants(group: Group?, participantsIds: List<String>): List<String> {
        return group?.let {
            val localGroupIds = group.memberIds
            participantsIds.filter { !localGroupIds.contains(it) }
        } ?: emptyList()
    }
}