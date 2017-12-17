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

import android.support.annotation.IntDef
import com.toshi.R
import com.toshi.view.BaseApplication

class LocalStatusMessage(val type: Long, val sender: User?) {
    companion object {
        const val NEW_GROUP = 0L
        const val USER_LEFT = 1L
    }

    fun loadString(): String {
        return when (type) {
            NEW_GROUP -> BaseApplication.get().getString(R.string.lsm_group_created)
            USER_LEFT -> formatUserLeftMessage()
            else -> ""
        }
    }

    private fun formatUserLeftMessage() =
            String.format(BaseApplication.get().getString(R.string.lsm_user_left), sender?.displayName)

    @IntDef(NEW_GROUP, USER_LEFT)
    annotation class Type
}