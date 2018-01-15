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

package com.toshi.util.statusMessage

import com.toshi.model.local.Group
import com.toshi.model.local.LocalStatusMessage
import com.toshi.model.local.User
import com.toshi.model.sofa.SofaAdapters
import com.toshi.model.sofa.SofaMessage

class StatusMessageBuilder {

    companion object {

        @JvmStatic
        fun buildGroupCreatedStatusMessage(): SofaMessage {
            val localStatusMessage = LocalStatusMessage(LocalStatusMessage.NEW_GROUP)
            val localStatusMessageJson = SofaAdapters.get().toJson(localStatusMessage)
            return SofaMessage().makeNewLocalStatusMessage(localStatusMessageJson)
        }

        @JvmStatic
        fun buildUserLeftStatusMessage(sender: User): SofaMessage {
            val localStatusMessage = LocalStatusMessage(LocalStatusMessage.USER_LEFT, sender)
            val localStatusMessageJson = SofaAdapters.get().toJson(localStatusMessage)
            return SofaMessage().makeNewLocalStatusMessage(localStatusMessageJson)
        }

        @JvmStatic
        fun buildAddStatusMessage(sender: User?, newUsers: List<User>): SofaMessage? {
            if (newUsers.isEmpty()) return null
            val localStatusMessage = LocalStatusMessage(LocalStatusMessage.USER_ADDED, sender, newUsers)
            val localStatusMessageJson = SofaAdapters.get().toJson(localStatusMessage)
            return SofaMessage().makeNewLocalStatusMessage(localStatusMessageJson)
        }

        @JvmStatic
        fun buildAddedToGroupStatusMessage(group: Group): SofaMessage {
            val localStatusMessage = LocalStatusMessage(LocalStatusMessage.ADDED_TO_GROUP, group.title)
            val localStatusMessageJson = SofaAdapters.get().toJson(localStatusMessage)
            return SofaMessage().makeNewLocalStatusMessage(localStatusMessageJson)
        }

        @JvmStatic
        fun addGroupNameUpdatedStatusMessage(sender: User?, updatedGroupName: String): SofaMessage {
            val localStatusMessage = LocalStatusMessage(LocalStatusMessage.GROUP_NAME_UPDATED, sender, updatedGroupName)
            val localStatusMessageJson = SofaAdapters.get().toJson(localStatusMessage)
            return SofaMessage().makeNewLocalStatusMessage(localStatusMessageJson)
        }
    }
}