/*
 * Copyright (c) 2017. Toshi Inc
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

package com.toshi.managers.tokenManager

import com.toshi.managers.balanceManager.TestTokenBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class TokenManagerTests {

    private val tokenManager = TokenManagerMocker().mock()
    private val testTokenBuilder by lazy { TestTokenBuilder() }

    @Test
    fun testGetERC20Tokens() {
        val erc20Tokens = tokenManager
                .getERC20Tokens()
                .toBlocking()
                .value()

        val expectedTokenList = testTokenBuilder.createERC20TokenList()
        assertEquals(erc20Tokens, expectedTokenList)
    }

    @Test
    fun testGetERC20Token() {
        val erc20Token = tokenManager
                .getERC20Token(testTokenBuilder.tokenContractAddress)
                .toBlocking()
                .value()

        val expectedERC20Token = testTokenBuilder.createERC20Token()
        assertEquals(erc20Token, expectedERC20Token)
    }

    @Test
    fun testGetERC721Tokens() {
        val erc721Tokens = tokenManager
                .getERC721Tokens()
                .toBlocking()
                .value()

        val expectedERC721Tokens = testTokenBuilder.createERC721TokenList()
        assertEquals(erc721Tokens, expectedERC721Tokens)
    }

    @Test
    fun testGetERC721Token() {
        val erc721Token = tokenManager
                .getERC721Token(testTokenBuilder.tokenContractAddress)
                .toBlocking()
                .value()

        val expectedERC721Token = testTokenBuilder.createERC721Token()
        assertEquals(erc721Token, expectedERC721Token)
    }
}