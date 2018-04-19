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

package com.toshi.util

import com.toshi.extensions.getQueryMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringQueryTests {

    private val invalidStrings by lazy {
        listOf(
                "",
                "search",
                "search&",
                "search?user123",
                "search&user123",
                "i=",
                "=i"
        )
    }

    private val validStrings by lazy {
        listOf(
                "i=e",
                "search=user123",
                "search=user123&type=user",
                "search=user123&type=user&query=something"
        )
    }

    @Test
    fun testInvalidStrings() {
        for (string in invalidStrings) {
            val queryMap = string.getQueryMap()
            assertTrue(queryMap.size == 0)
        }
    }

    @Test
    fun testValidStringsWithShortSingleParams() {
        val queryMap = validStrings[0].getQueryMap()
        assertTrue(queryMap.size == 1)
        assertEquals(queryMap["i"], "e")
    }

    @Test
    fun testValidStringsWithSingleParams() {
        val queryMap = validStrings[1].getQueryMap()
        assertTrue(queryMap.size == 1)
        assertEquals(queryMap["search"], "user123")
    }

    @Test
    fun testValidStringsWithTwoParams() {
        val queryMap = validStrings[2].getQueryMap()
        assertTrue(queryMap.size == 2)
        assertEquals(queryMap["search"], "user123")
        assertEquals(queryMap["type"], "user")
    }

    @Test
    fun testValidStringsWithThreeParams() {
        val queryMap = validStrings[3].getQueryMap()
        assertEquals(queryMap["search"], "user123")
        assertEquals(queryMap["type"], "user")
        assertEquals(queryMap["query"], "something")
    }
}