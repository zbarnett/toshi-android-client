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

package com.toshi.managers

import com.toshi.model.network.ERC721TokenWrapper
import com.toshi.model.network.token.ERC20Tokens
import com.toshi.model.network.token.ERC721Tokens
import com.toshi.model.network.token.ERCToken

class TestTokenBuilder {

    val contractAddress = "0xd26114cd6EE289AccF82350c8d8487fedB8A0C07"

    fun createERC20Token(): ERCToken = createERC20TokenList().tokens.first()

    fun createERC20TokenList(): ERC20Tokens {
        val listOfERC20Tokens = listOf(
                ERCToken(
                        symbol = "OMG",
                        name = "OMGCoin",
                        value = "1",
                        decimals = 18,
                        contractAddress = contractAddress,
                        icon = null
                ),
                ERCToken(
                        symbol = "LOL",
                        name = "LOLCoin",
                        value = "10",
                        decimals = 18,
                        contractAddress = contractAddress,
                        icon = null
                )
        )
        return ERC20Tokens(listOfERC20Tokens)
    }

    fun createERC721TokenList(): ERC721Tokens {
        val listOfERC721Tokens = listOf(
                ERCToken(
                        symbol = null,
                        name = "CryptoKitties",
                        value = "10",
                        decimals = 0,
                        contractAddress = contractAddress,
                        icon = null
                ),
                ERCToken(
                        symbol = null,
                        name = "CryptoPunks",
                        value = "11",
                        decimals = 0,
                        contractAddress = contractAddress,
                        icon = null
                )
        )
        return ERC721Tokens(listOfERC721Tokens)
    }

    fun createERC721Token(): ERC721TokenWrapper {
        return ERC721TokenWrapper(
                name = "CryptoKitties",
                value = "10",
                contractAddress = contractAddress,
                icon = null,
                tokens = null,
                type = null,
                url = null
        )
    }
}