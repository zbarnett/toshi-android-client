/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 *  This program is free software: you can redistribute it and/or modify
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

package com.toshi.extensions

import android.content.SharedPreferences
import android.util.Base64

fun SharedPreferences.applyString(key: String, value: String?) = edit().putString(key, value).apply()

fun SharedPreferences.commitString(key: String, value: String?) = edit().putString(key, value).commit()

fun SharedPreferences.getString(key: String): String? = getString(key, null)

fun SharedPreferences.applyBoolean(key: String, value: Boolean = false) = edit().putBoolean(key, value).apply()

fun SharedPreferences.commitBoolean(key: String, value: Boolean = false) = edit().putBoolean(key, value).commit()

fun SharedPreferences.getBoolean(key: String): Boolean = getBoolean(key, false)

fun SharedPreferences.applyInt(key: String, value: Int = 0) = edit().putInt(key, value).apply()

fun SharedPreferences.commitInt(key: String, value: Int = 0) = edit().putInt(key, value).commit()

fun SharedPreferences.getInt(key: String): Int = getInt(key, 0)

fun SharedPreferences.applyClear() = edit().clear().apply()

fun SharedPreferences.applyByteArray(key: String, value: ByteArray) {
    val encoded = Base64.encodeToString(value, Base64.NO_WRAP)
    applyString(key, encoded)
}

fun SharedPreferences.getByteArray(key: String): ByteArray? {
    val encoded = getString(key) ?: return null
    return Base64.decode(encoded, Base64.NO_WRAP)
}