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
import com.toshi.model.local.Recipient
import com.toshi.model.local.User
import com.toshi.util.LogUtil
import com.toshi.view.BaseApplication
import rx.Completable
import rx.Single

class NewGroupNameTask(val conversationStore: ConversationStore) {

    private val recipientManager by lazy { BaseApplication.get().recipientManager }

    fun run(messageSource: String, groupId: String, newGroupName: String): Completable {
        return Single.zip(
                recipientManager.getGroupFromId(groupId),
                recipientManager.getUserFromToshiId(messageSource),
                { group, sender -> Pair(Recipient(group), sender) }
        )
        .flatMapCompletable { addGroupNameUpdatedStatusMessage(it.first, it.second, newGroupName) }
        .doOnError { LogUtil.e(javaClass, "Error while adding group info updated status message $it") }
    }

    private fun addGroupNameUpdatedStatusMessage(recipient: Recipient, sender: User, newGroupName: String): Completable {
        return if (newGroupName != recipient.group.title) conversationStore.addGroupNameUpdatedStatusMessage(recipient, sender, newGroupName)
        else Completable.complete()
    }
}