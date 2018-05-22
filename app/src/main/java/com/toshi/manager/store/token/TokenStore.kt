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

package com.toshi.manager.store.token

import com.toshi.model.network.token.ERC20Token
import com.toshi.view.BaseApplication
import rx.Scheduler
import rx.Single
import rx.schedulers.Schedulers
import java.util.concurrent.Executors

class TokenStore(
        private val baseApplication: BaseApplication,
        private val scheduler: Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())
) : TokenStoreInterface {

    override fun saveAllTokens(tokens: List<ERC20Token>, networkId: String, walletIndex: Int): Single<List<ERC20Token>> {
        return Single.fromCallable { save(tokens, networkId, walletIndex) }
                .subscribeOn(scheduler)
    }

    private fun save(tokens: List<ERC20Token>, networkId: String, walletIndex: Int): List<ERC20Token> {
        val tokensWithPrimaryKeys = createListWithPrimaryKeys(tokens, networkId, walletIndex)
        val realm = baseApplication.realm
        realm.executeTransaction { realm.insertOrUpdate(tokensWithPrimaryKeys) }
        realm.close()
        return tokensWithPrimaryKeys
    }

    override fun getAllTokens(networkId: String, walletIndex: Int): Single<List<ERC20Token>> {
        return Single.fromCallable { getAll(networkId, walletIndex) }
                .subscribeOn(scheduler)
    }

    private fun getAll(networkId: String, walletIndex: Int): List<ERC20Token> {
        val realm = BaseApplication.get().realm
        val result = realm
                .where(ERC20Token::class.java)
                .equalTo("networkId", networkId)
                .and()
                .equalTo("walletIndex", walletIndex)
                .findAll()

        val isEmptyResult = result != null && result.isNotEmpty()
        val tokens = if (isEmptyResult) realm.copyFromRealm(result) else emptyList()
        realm.close()
        return tokens
    }
}