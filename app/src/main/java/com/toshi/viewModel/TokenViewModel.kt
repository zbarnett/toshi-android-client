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

package com.toshi.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.toshi.R
import com.toshi.model.local.token.ERCTokenView
import com.toshi.model.local.token.EtherToken
import com.toshi.model.local.token.Token
import com.toshi.model.network.Balance
import com.toshi.model.network.token.ERC20Token
import com.toshi.model.network.token.ERC20Tokens
import com.toshi.util.EthUtil
import com.toshi.util.SingleLiveEvent
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class TokenViewModel : ViewModel() {

    private val transactionManager by lazy { BaseApplication.get().transactionManager }
    private val balanceManager by lazy { BaseApplication.get().balanceManager }
    private val subscriptions by lazy { CompositeSubscription() }

    val erc20Tokens by lazy { MutableLiveData<List<Token>>() }
    val erc721Tokens by lazy { MutableLiveData<List<Token>>() }
    val erc20error by lazy { SingleLiveEvent<Int>() }
    val erc721error by lazy { SingleLiveEvent<Int>() }
    val isERC20Loading by lazy { MutableLiveData<Boolean>() }
    val isERC721Loading by lazy { MutableLiveData<Boolean>() }

    init {
        listenForBalanceUpdates()
        listenForNewIncomingTokenPayments()
        firstFetchERC20Tokens()
        firstFetchERC721Tokens()
    }

    private fun listenForBalanceUpdates() {
        val sub = balanceManager
                .balanceObservable
                .skip(1)
                .filter { it != null }
                .flatMap { it.getBalanceWithLocalBalance().toObservable() }
                .map { mapBalance(it) }
                .flatMap { fetchERC20TokensAndAddEther(it).toObservable() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { erc20Tokens.value = it },
                        { LogUtil.exception("Error during fetching balance $it") }
                )

        subscriptions.add(sub)
    }

    private fun listenForNewIncomingTokenPayments() {
        val sub = transactionManager
                .listenForNewIncomingTokenPayments()
                .flatMap { fetchERC20TokensAndAddEther().toObservable() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { erc20Tokens.value = it },
                        { erc20error.value = R.string.error_fetching_tokens }
                )

        subscriptions.add(sub)
    }

    private fun firstFetchERC20Tokens() {
        val sub = fetchERC20TokensAndAddEther()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isERC20Loading.value = true }
                .doAfterTerminate { isERC20Loading.value = false }
                .subscribe(
                        { erc20Tokens.value = it },
                        { erc20error.value = R.string.error_fetching_tokens }
                )

        subscriptions.add(sub)
    }

    fun fetchERC20Tokens() {
        val sub = fetchERC20TokensAndAddEther()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { erc20Tokens.value = it },
                        { erc20error.value = R.string.error_fetching_tokens }
                )

        subscriptions.add(sub)
    }

    private fun fetchERC20TokensAndAddEther(): Single<List<Token>> {
        return Single.zip(
                getERC20Tokens(),
                createEtherToken(),
                { tokens, etherToken -> Pair(tokens, etherToken) }
        )
        .map { addEtherTokenToTokenList(it.first, it.second) }
    }

    private fun fetchERC20TokensAndAddEther(etherToken: EtherToken): Single<List<Token>> {
        return getERC20Tokens()
                .map { Pair(it, etherToken) }
                .map { addEtherTokenToTokenList(it.first, it.second) }
    }

    private fun getERC20Tokens(): Single<List<ERCTokenView>> {
        return balanceManager
                .getERC20Tokens()
                .onErrorReturn { ERC20Tokens().tokens }
                .map { mapERC20Tokens(it) }
    }

    private fun mapERC20Tokens(ERC20Tokens: List<ERC20Token>): List<ERCTokenView> {
        return ERC20Tokens.map {
            ERCTokenView(
                    symbol = it.symbol,
                    name = it.name,
                    balance = it.balance,
                    decimals = it.decimals,
                    contractAddress = it.contractAddress,
                    icon = it.icon
            )
        }
    }

    private fun addEtherTokenToTokenList(tokens: List<ERCTokenView>, etherToken: EtherToken): List<Token> {
        val tokenList = mutableListOf<Token>()
        tokenList.add(etherToken)
        tokenList.addAll(tokens)
        return tokenList
    }

    private fun createEtherToken(): Single<EtherToken> {
        return balanceManager.balanceObservable
                .first()
                .toSingle()
                .flatMap { it.getBalanceWithLocalBalance() }
                .map { mapBalance(it) }
    }

    private fun mapBalance(balance: Balance): EtherToken {
        val ethAmount = EthUtil.weiAmountToUserVisibleString(balance.unconfirmedBalance)
        return EtherToken.create(etherValue = ethAmount, fiatValue = balance.localBalance ?: "0.00")
    }

    private fun firstFetchERC721Tokens() {
        val sub = balanceManager
                .getERC721Tokens()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isERC721Loading.value = true }
                .doAfterTerminate { isERC721Loading.value = false }
                .subscribe(
                        { erc721Tokens.value = it.collectibles },
                        { erc721error.value = R.string.error_fetching_tokens }
                )

        subscriptions.add(sub)
    }

    fun fetchERC721Tokens() {
        val sub = balanceManager
                .getERC721Tokens()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { erc721Tokens.value = it.collectibles },
                        { erc721error.value = R.string.error_fetching_tokens }
                )

        subscriptions.add(sub)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}