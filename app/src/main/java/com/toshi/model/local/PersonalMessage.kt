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

import com.squareup.moshi.Moshi
import com.toshi.crypto.util.TypeConverter
import java.io.IOException

data class PersonalMessage(val from: String, var data: String) {

    companion object {
        @Throws(IOException::class)
        @JvmStatic fun build(msgParams: String): PersonalMessage {
            val personalMessageSignJsonAdapter = Moshi.Builder().build().adapter(PersonalMessage::class.java)
            return personalMessageSignJsonAdapter.fromJson(msgParams)
        }
    }

    fun getDataFromMessageAsString(): String {
        val bytes = TypeConverter.StringHexToByteArray(data)
        return String(bytes)
    }

    fun getDataFromMessageAsBytes(): ByteArray = TypeConverter.StringHexToByteArray(data)
}