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

package com.toshi.model.local

import com.toshi.R
import com.toshi.view.BaseApplication

class LocalStatusMessage(
        val type: Long,
        val sender: User?,
        val newUsers: List<User>?,
        val groupName: String?
) {

    constructor(type: Long) : this(type, null, null, null)
    constructor(type: Long, sender: User?) : this(type, sender, null, null)
    constructor(type: Long, sender: User?, newUsers: List<User>) : this(type, sender, newUsers, null)
    constructor(type: Long, groupName: String) : this(type, null, null, groupName)
    constructor(type: Long, sender: User?, groupName: String) : this(type, sender, null, groupName)

    companion object {
        const val NEW_GROUP = 0L
        const val USER_LEFT = 1L
        const val USER_ADDED = 2L
        const val ADDED_TO_GROUP = 3L
        const val GROUP_NAME_UPDATED = 4L
    }

    fun loadString(isSenderLocalUser: Boolean): String {
        return when (type) {
            NEW_GROUP -> BaseApplication.get().getString(R.string.lsm_group_created)
            USER_LEFT -> formatUserLeftMessage()
            USER_ADDED -> formatUserAddedMessage(isSenderLocalUser)
            ADDED_TO_GROUP -> formatAddedToGroup()
            GROUP_NAME_UPDATED -> formatGroupNameUpdated(isSenderLocalUser)
            else -> ""
        }
    }

    private fun formatUserLeftMessage() = String.format(BaseApplication.get().getString(R.string.lsm_user_left), sender?.displayName)

    private fun formatUserAddedMessage(isSenderLocalUser: Boolean): String {
        val displayNameOfSender = if (isSenderLocalUser) BaseApplication.get().getString(R.string.you)
        else sender?.displayName ?: ""

        return newUsers?.let {
            return when (newUsers.size) {
                0 -> ""
                1 -> formatUserAddedString(isSenderLocalUser, newUsers[0])
                in 2..3 -> {
                    val firstUsers = joinToString(newUsers.size - 1, newUsers)
                    val lastUser = newUsers.last().displayName
                    sender?.let {
                        BaseApplication.get().getString(R.string.lsm_added_users, displayNameOfSender, firstUsers, lastUser)
                    } ?: BaseApplication.get().getString(R.string.lsm_added_users_without_sender, firstUsers, lastUser)
                }
                else -> {
                    val numberOfNamesToShow = 2
                    val firstUsers = joinToString(numberOfNamesToShow, newUsers)
                    val numberOfLeftoverUsers = newUsers.size - numberOfNamesToShow
                    sender?.let {
                        BaseApplication.get().getString(R.string.lsm_added_users_wrapped, displayNameOfSender, firstUsers, numberOfLeftoverUsers)
                    } ?: BaseApplication.get().getString(R.string.lsm_added_users_wrapped_without_sender, firstUsers, numberOfLeftoverUsers)
                }
            }
        } ?: ""
    }

    private fun formatUserAddedString(isSenderLocalUser: Boolean, newUser: User): String {
        return sender?.let { formatUserAddedWithSender(isSenderLocalUser, it, newUser) }
                ?: formatUserAddedWithoutSender(newUser)
    }

    private fun formatUserAddedWithSender(isSenderLocalUser: Boolean, sender: User, newUser: User): String {
        val displayNameOfSender = if (isSenderLocalUser) BaseApplication.get().getString(R.string.you) else sender.displayName
        return BaseApplication.get().getString(R.string.lsm_added_user, displayNameOfSender, newUser.displayName)
    }

    private fun formatUserAddedWithoutSender(newUser: User): String {
        return BaseApplication.get().getString(R.string.lsm_added_user_without_sender, newUser.displayName)
    }

    private fun joinToString(n: Int, newUsers: List<User>): String {
        return newUsers
                .take(n)
                .joinToString(separator = ", ") { it.displayName }
    }

    private fun formatAddedToGroup() = BaseApplication.get().getString(R.string.lsm_added_to_group, groupName ?: "")

    private fun formatGroupNameUpdated(isSenderLocalUser: Boolean): String {
        return sender?.let { formatGroupNameUpdatedWithSender(isSenderLocalUser, it) }
                ?: formatGroupNameUpdatedWithoutSender()
    }

    private fun formatGroupNameUpdatedWithSender(isSenderLocalUser: Boolean, sender: User): String {
        val displayNameOfSender = if (isSenderLocalUser) BaseApplication.get().getString(R.string.you) else sender.displayName
        return BaseApplication.get().getString(R.string.lsm_group_info_updated, displayNameOfSender, groupName ?: "")
    }

    private fun formatGroupNameUpdatedWithoutSender(): String {
        return BaseApplication.get().getString(R.string.lsm_group_info_updated_without_sender, groupName ?: "")
    }
}