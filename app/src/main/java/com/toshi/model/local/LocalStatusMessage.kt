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

class LocalStatusMessage {
    companion object {
        const val NEW_GROUP = 0L

        fun loadString(type: String): String {
            val asLong = type.toLongOrNull()
            return asLong?.let { loadString(asLong) } ?: ""
        }

        private fun loadString(@LocalStatusMessage.Type type: Long): String {
            return when (type) {
                LocalStatusMessage.NEW_GROUP -> BaseApplication.get().getString(R.string.lsm_group_created)
                else -> ""
            }
        }
    }

    @IntDef(NEW_GROUP)
    annotation class Type
}