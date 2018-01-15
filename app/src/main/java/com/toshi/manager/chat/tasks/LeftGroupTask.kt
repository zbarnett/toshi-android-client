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

import com.toshi.extensions.NETWORK_TIMEOUT_SECONDS
import com.toshi.manager.store.ConversationStore
import com.toshi.model.local.User
import com.toshi.util.LogUtil
import com.toshi.view.BaseApplication
import org.spongycastle.util.encoders.Hex
import org.whispersystems.signalservice.api.messages.SignalServiceGroup
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class LeftGroupTask(private val conversationStore: ConversationStore) {

    private val recipientManager by lazy { BaseApplication.get().recipientManager }

    fun run(messageSource: String, signalGroup: SignalServiceGroup) {
        val user = getUserFromToshiId(messageSource)
        val groupId = Hex.toHexString(signalGroup.groupId)
        user?.let {
            conversationStore.removeUserFromGroup(groupId, user).await()
        } ?: LogUtil.e(javaClass, "User is null when handling leave message")
    }

    private fun getUserFromToshiId(id: String): User? {
        return try {
            recipientManager
                    .getUserFromToshiId(id)
                    .timeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .toBlocking()
                    .value()
        } catch (e: TimeoutException) {
            LogUtil.e(javaClass, "Error when trying to fetch user $e")
            null
        }
    }
}