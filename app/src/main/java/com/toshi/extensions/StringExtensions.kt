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
@file:JvmName("StringUtils")
package com.toshi.extensions

import android.util.Patterns
import com.toshi.model.local.Group.GROUP_ID_LENGTH

fun String.isGroupId(): Boolean {
    // Todo - check compatability with other clients (i.e. iOS)
    return length == GROUP_ID_LENGTH
}

fun String.isWebUrl() = Patterns.WEB_URL.matcher(this.trim()).matches()

fun String.findTypeParamValue(): String? {
    val regexResult = Regex("type=([a-zA-Z]+)", RegexOption.IGNORE_CASE)
            .find(this)
    if (regexResult?.groups?.size != 2) return null
    return regexResult.groups[1]?.value
}

fun String.getQueryMap(): HashMap<String, String> {
    val minLength = 3
    if (this.length < minLength || !this.contains("=")) return HashMap()
    val params = this.split("&")
    val map = HashMap<String, String>()
    for (param in params) {
        val stringSplitter = param.split("=")
        val name = stringSplitter[0]
        val value = stringSplitter[1]
        map[name] = value
    }
    return map
}