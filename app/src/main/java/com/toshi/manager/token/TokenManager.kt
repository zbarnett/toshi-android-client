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

package com.toshi.manager.token

import com.toshi.crypto.HDWallet
import com.toshi.extensions.getTimeoutSingle
import com.toshi.manager.network.EthereumService
import com.toshi.manager.network.EthereumServiceInterface
import com.toshi.manager.store.TokenStore
import com.toshi.model.local.network.Networks
import com.toshi.model.network.token.CustomERCToken
import com.toshi.model.network.token.ERC20Token
import com.toshi.model.network.token.ERC721TokenWrapper
import com.toshi.model.network.token.ERC721Tokens
import com.toshi.view.BaseApplication
import rx.Completable
import rx.Observable
import rx.Scheduler
import rx.Single
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

class TokenManager(
        private val tokenStore: TokenStore = TokenStore(BaseApplication.get()),
        private val networks: Networks = Networks.getInstance(),
        private val ethService: EthereumServiceInterface = EthereumService,
        private val walletObservable: Observable<HDWallet>,
        private val connectivitySubject: BehaviorSubject<Boolean>,
        private val scheduler: Scheduler = Schedulers.io()
) {
    fun getERC20Tokens(): Single<List<ERC20Token>> {
        return getWallet()
                .flatMap { getERC20Tokens(it.getCurrentWalletIndex(), networks.currentNetwork.id) }
    }

    private fun getERC20Tokens(walletIndex: Int, networkId: String): Single<List<ERC20Token>> {
        return Single.concat(
                tokenStore.getAllTokens(networkId = networkId, walletIndex = walletIndex),
                getERC20TokensFromNetwork()
        )
        .first { areTokensFresh(it) }
        .toSingle()
    }

    fun getERC20TokensFromNetwork(): Single<List<ERC20Token>> {
        return getWallet()
                .flatMap { ethService.get().getTokens(it.paymentAddress) }
                .map { it.tokens }
                .flatMap { saveERC20Tokens(it) }
                .subscribeOn(scheduler)
    }

    private fun saveERC20Tokens(ERC20Tokens: List<ERC20Token>): Single<List<ERC20Token>> {
        return getWallet()
                .flatMap { saveERC20Tokens(ERC20Tokens, it.getCurrentWalletIndex(), networks.currentNetwork.id) }
    }

    private fun saveERC20Tokens(ERC20Tokens: List<ERC20Token>, walletIndex: Int, networkId: String): Single<List<ERC20Token>> {
        return tokenStore.saveAllTokens(ERC20Tokens, networkId, walletIndex)
    }

    private fun areTokensFresh(ERC20Tokens: List<ERC20Token>): Boolean {
        return when {
            ERC20Tokens.isEmpty() -> false
            !isConnected() -> true
            else -> !ERC20Tokens[0].needsRefresh()
        }
    }

    private fun isConnected(): Boolean = connectivitySubject.value ?: false

    fun getERC20Token(contractAddress: String): Single<ERC20Token> {
        return getWallet()
                .flatMap { ethService.get().getToken(it.paymentAddress, contractAddress) }
                .subscribeOn(scheduler)
    }

    fun getERC721Tokens(): Single<ERC721Tokens> {
        return getWallet()
                .flatMap { ethService.get().getCollectibles(it.paymentAddress) }
                .subscribeOn(scheduler)
    }

    fun getERC721Token(contactAddress: String): Single<ERC721TokenWrapper> {
        return getWallet()
                .flatMap { ethService.get().getCollectible(it.paymentAddress, contactAddress) }
                .subscribeOn(scheduler)
    }

    fun addCustomToken(customERCToken: CustomERCToken): Completable {
        return ethService
                .get()
                .timestamp
                .flatMapCompletable { ethService.get().addCustomToken(it.get(), customERCToken) }
                .subscribeOn(scheduler)
    }

    private fun getWallet(): Single<HDWallet> {
        return walletObservable
                .getTimeoutSingle()
                .subscribeOn(scheduler)
    }
}