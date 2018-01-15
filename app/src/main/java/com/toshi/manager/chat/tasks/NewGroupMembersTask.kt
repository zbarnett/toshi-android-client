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
import com.toshi.model.local.Recipient
import com.toshi.model.local.User
import com.toshi.util.LogUtil
import com.toshi.view.BaseApplication
import rx.Completable
import rx.Observable
import rx.Single

class NewGroupMembersTask(
        private val conversationStore: ConversationStore,
        private val addStatusMessage: Boolean = true
) {

    private val recipientManager by lazy { BaseApplication.get().recipientManager }

    fun run(groupId: String, senderId: String, memberIds: List<String>): Completable {
        return recipientManager
                .getGroupFromId(groupId)
                .map { Recipient(it) }
                .flatMapCompletable { addNewGroupMembersStatusMessage(it, senderId, memberIds) }
                .doOnError { LogUtil.e(javaClass, "Error while updating group members $it") }
    }

    private fun addNewGroupMembersStatusMessage(recipient: Recipient?, senderId: String, memberIds: List<String>): Completable {
        val newMemberIds = findNewGroupMembers(recipient?.group, memberIds)
        if (newMemberIds.isEmpty()) return Completable.complete()
        return Single.zip(
                getUserFromId(senderId),
                getNewMembers(newMemberIds),
                { user, newMembers -> Pair(user, newMembers) }
        )
        .flatMapCompletable { addStatusMessageAndUpdateGroup(recipient, it.first, it.second) }
    }

    private fun getNewMembers(newUsers: List<String>): Single<List<User>> {
        return Observable.from(newUsers)
                .flatMap { getUserFromId(it).toObservable() }
                .toList()
                .toSingle()
    }

    private fun getUserFromId(id: String) = recipientManager.getUserFromToshiId(id)

    private fun findNewGroupMembers(group: Group?, memberIds: List<String>): List<String> {
        return group?.let {
            val localGroupIds = group.memberIds
            memberIds.filter { !localGroupIds.contains(it) }
        } ?: emptyList()
    }

    private fun addStatusMessageAndUpdateGroup(recipient: Recipient?, sender: User, newMembers: List<User>): Completable {
        return recipient?.group?.let {
            if (addStatusMessage) {
                addStatusMessage(recipient, sender, newMembers)
                        .andThen(updateGroup(it, newMembers))
            } else updateGroup(it, newMembers)
        } ?: Completable.error(Throwable("Recipient/Group is null"))
    }

    private fun addStatusMessage(recipient: Recipient, sender: User, newUsers: List<User>): Completable {
        return conversationStore
                .addNewGroupMembersStatusMessage(recipient, sender, newUsers)
                .toCompletable()
    }

    private fun updateGroup(group: Group, newMembers: List<User>): Completable {
        return conversationStore.addNewMembersToGroup(group.id, newMembers)
    }
}