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

import com.toshi.extensions.findTypeParamValue
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeParamTests {

    private val invalidStrings by lazy {
        listOf(
                "",
                "type=1",
                "ype=user",
                "type?user"
        )
    }

    private val validStrings by lazy {
        listOf(
                "type=user",
                "type=bot",
                "type=groupbot",
                "TYPE=groupbot",
                "type=GROUPBOT",
                "type=r",
                "type=user&query=user123",
                "query=user123&type=user",
                "query=user123&type=groupbot&something=123"
        )
    }

    @Test
    fun findTypeParamInInvalidStrings() {
        for (string in invalidStrings) {
            assertEquals(null, string.findTypeParamValue())
        }
    }

    @Test
    fun `find user type param`() {
        assertEquals("user", validStrings[0].findTypeParamValue())
    }

    @Test
    fun `find bot type param`() {
        assertEquals("bot", validStrings[1].findTypeParamValue())
    }

    @Test
    fun `find groupbot type param`() {
        assertEquals("groupbot", validStrings[2].findTypeParamValue())
    }

    @Test
    fun `find groupbot type param with uppercase param name`() {
        assertEquals("groupbot", validStrings[3].findTypeParamValue())
    }

    @Test
    fun `find groupbot type param with uppercase param value`() {
        assertEquals("GROUPBOT", validStrings[4].findTypeParamValue())
    }

    @Test
    fun `find r type param`() {
        assertEquals("r", validStrings[5].findTypeParamValue())
    }

    @Test
    fun `find user type param with two params`() {
        assertEquals("user", validStrings[6].findTypeParamValue())
    }

    @Test
    fun `find user type as last param with two params`() {
        assertEquals("user", validStrings[7].findTypeParamValue())
    }

    @Test
    fun `find groupbot type as last param with three params`() {
        assertEquals("groupbot", validStrings[8].findTypeParamValue())
    }
}