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

package com.toshi.testStores

import com.toshi.manager.store.token.TokenStoreInterface
import com.toshi.model.network.token.ERC20Token
import rx.Single

class TestTokenStore : TokenStoreInterface {

    private val tokens = mutableListOf<ERC20Token>()

    override fun getAllTokens(networkId: String, walletIndex: Int): Single<List<ERC20Token>> {
        return Single.fromCallable {
            return@fromCallable tokens.filter { it.networkId == networkId && it.walletIndex == walletIndex }
        }
    }

    override fun saveAllTokens(tokens: List<ERC20Token>, networkId: String, walletIndex: Int): Single<List<ERC20Token>> {
        return Single.fromCallable {
            val listWithPrimaryKeys = createListWithPrimaryKeys(tokens, networkId, walletIndex)
            this.tokens.forEachIndexed { index, storedToken ->
                listWithPrimaryKeys.forEach { newToken ->
                    if (storedToken.primaryKey.equals(newToken.primaryKey)) this.tokens[index] = newToken
                    else this.tokens.add(newToken)
                }
            }
            return@fromCallable tokens
        }
    }
}