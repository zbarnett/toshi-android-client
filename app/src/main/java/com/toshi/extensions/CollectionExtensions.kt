package com.toshi.extensions

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

fun <K, V> Map<K, V>.toArrayList(): ArrayList<Pair<K, V>> {
    val list = ArrayList<Pair<K, V>>()
    forEach { list.add(Pair(it.key, it.value)) }
    return list
}

fun <K, V> ArrayList<Pair<K, V>>.toMap(): Map<K, V> {
    val map = mutableMapOf<K, V>()
    forEach { map[it.first] = it.second }
    return map
}