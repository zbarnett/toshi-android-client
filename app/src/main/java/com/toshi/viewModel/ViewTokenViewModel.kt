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
import com.toshi.manager.TransactionManager
import com.toshi.manager.token.TokenManager
import com.toshi.model.local.network.Networks
import com.toshi.model.local.token.ERC20TokenView
import com.toshi.model.local.token.Token
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ViewTokenViewModel(
        token: Token,
        private val baseApplication: BaseApplication = BaseApplication.get(),
        private val transactionManager: TransactionManager = baseApplication.transactionManager,
        private val tokenManager: TokenManager = baseApplication.tokenManager
) : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    val token by lazy { MutableLiveData<Token>() }

    init {
        this.token.value = token
        if (token is ERC20TokenView) listenForNewIncomingTokenPayments(token)
    }

    private fun listenForNewIncomingTokenPayments(ERCToken: ERC20TokenView) {
        val sub = transactionManager
                .listenForNewIncomingTokenPayments()
                .filter { it.contractAddress == ERCToken.contractAddress }
                .flatMap { getERC20Token(it.contractAddress).toObservable() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { this.token.value = it },
                        { LogUtil.exception("Error while listening to token updates $it") }
                )

        subscriptions.add(sub)
    }

    private fun getERC20Token(contractAddress: String): Single<ERC20TokenView> {
        return tokenManager
                .getERC20Token(contractAddress)
                .map { ERC20TokenView.map(it) }
    }

    fun getNetworks() = Networks.getInstance()

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}