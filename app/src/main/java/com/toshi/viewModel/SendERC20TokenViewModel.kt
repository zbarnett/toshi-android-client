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
import com.toshi.crypto.util.TypeConverter
import com.toshi.extensions.createSafeBigDecimal
import com.toshi.extensions.isValidDecimal
import com.toshi.manager.TransactionManager
import com.toshi.manager.token.TokenManager
import com.toshi.model.local.network.Networks
import com.toshi.model.local.token.ERCTokenView
import com.toshi.util.EthUtil
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.math.BigDecimal

class SendERC20TokenViewModel(
        val token: ERCTokenView,
        private val baseApplication: BaseApplication = BaseApplication.get(),
        private val transactionManager: TransactionManager = baseApplication.transactionManager,
        private val tokenManager: TokenManager = baseApplication.tokenManager
) : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    val ERCToken by lazy { MutableLiveData<ERCTokenView>() }
    val isSendingMaxAmount by lazy { MutableLiveData<Boolean>() }

    init {
        ERCToken.value = token
        listenForNewIncomingTokenPayments()
    }

    private fun listenForNewIncomingTokenPayments() {
        val sub = transactionManager
                .listenForNewIncomingTokenPayments()
                .filter { it.contractAddress == token.contractAddress }
                .flatMap { getERC20Token(it.contractAddress).toObservable() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { ERCToken.value = it },
                        { LogUtil.exception("Error while listening to token updates $it") }
                )

        subscriptions.add(sub)
    }

    private fun getERC20Token(contractAddress: String): Single<ERCTokenView> {
        return tokenManager
                .getERC20Token(contractAddress)
                .map { ERCTokenView.map(it) }
    }

    fun isAmountValid(inputAmount: String) = inputAmount.isNotEmpty() && isValidDecimal(inputAmount)

    fun hasEnoughBalance(amount: String): Boolean {
        val token = ERCToken.value
        val inputAmount = createSafeBigDecimal(amount)
        val balanceAmount = if (token != null) {
            BigDecimal(TypeConverter.formatHexString(token.balance, token.decimals ?: 0, null))
        } else BigDecimal(0)
        return inputAmount.compareTo(balanceAmount) == 0 || inputAmount.compareTo(balanceAmount) == -1
    }

    fun getMaxAmount(): String {
        val token = ERCToken.value
        return if (token != null) TypeConverter.formatHexString(token.balance, token.decimals ?: 0, null)
        else TypeConverter.formatNumber(0, EthUtil.ETH_FORMAT)
    }

    fun getBalanceAmount(): String {
        val token = ERCToken.value
        return if (token != null) TypeConverter.formatHexString(token.balance, token.decimals ?: 0, EthUtil.ETH_FORMAT)
        else TypeConverter.formatNumber(0, EthUtil.ETH_FORMAT)
    }

    fun getSymbol(): String {
        val token = ERCToken.value
        return if (token != null) token.symbol ?: ""
        else ""
    }

    fun getNetworks() = Networks.getInstance()

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}