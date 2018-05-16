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
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Pair
import com.toshi.R
import com.toshi.manager.model.ERC20TokenPaymentTask
import com.toshi.manager.model.ExternalPaymentTask
import com.toshi.manager.model.PaymentTask
import com.toshi.manager.model.ToshiPaymentTask
import com.toshi.manager.model.W3PaymentTask
import com.toshi.model.local.CurrencyMode
import com.toshi.model.local.OutgoingPaymentResult
import com.toshi.model.local.UnsignedW3Transaction
import com.toshi.model.local.network.Networks
import com.toshi.model.network.Balance
import com.toshi.model.sofa.SofaAdapters
import com.toshi.util.EthUtil
import com.toshi.util.SingleLiveEvent
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.CALLBACK_ID
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.CONFIRMATION_TYPE
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.CURRENCY_MODE
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.DAPP_FAVICON
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.DAPP_TITLE
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.DAPP_URL
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.ETH_AMOUNT
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.PAYMENT_ADDRESS
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.PAYMENT_TYPE
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.SEND_MAX_AMOUNT
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.TOKEN_ADDRESS
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.TOKEN_DECIMALS
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.TOKEN_SYMBOL
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.TOSHI_ID
import com.toshi.view.fragment.PaymentConfirmationFragment.Companion.UNSIGNED_TRANSACTION
import com.toshi.view.fragment.dialogFragment.PaymentConfirmationType.EXTERNAL
import com.toshi.view.fragment.dialogFragment.PaymentConfirmationType.TOKEN_PAYMENT
import com.toshi.view.fragment.dialogFragment.PaymentConfirmationType.TOSHI_PAYMENT
import com.toshi.view.fragment.dialogFragment.PaymentConfirmationType.TOSHI_PAYMENT_REQUEST
import com.toshi.view.fragment.dialogFragment.PaymentConfirmationType.WEB
import rx.Completable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

class PaymentConfirmationViewModel : ViewModel() {

    private val toshiManager by lazy { BaseApplication.get().toshiManager }
    private val recipientManager by lazy { BaseApplication.get().recipientManager }
    private val transactionManager by lazy { BaseApplication.get().transactionManager }
    private val balanceManager by lazy { BaseApplication.get().balanceManager }
    private val subscriptions by lazy { CompositeSubscription() }

    val isLoading by lazy { SingleLiveEvent<Boolean>() }
    val paymentTask by lazy { SingleLiveEvent<PaymentTask>() }
    val paymentTaskError by lazy { SingleLiveEvent<Unit>() }
    val balance by lazy { MutableLiveData<Balance>() }
    val paymentSuccess by lazy { SingleLiveEvent<PaymentTask>() }
    val paymentError by lazy { SingleLiveEvent<Int>() }
    val error by lazy { SingleLiveEvent<Int>() }
    val finish by lazy { SingleLiveEvent<Unit>() }

    var bundle: Bundle? = null

    init {
        listenForOutgoingPaymentS()
        getBalance()
    }

    private fun listenForOutgoingPaymentS() {
        val sub = transactionManager.getOutgoingPaymentResultObservable()
                .filter { it != null }
                .filter { it.paymentTask == paymentTask.value }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { isLoading.value = false }
                .subscribe(
                        { handleOutgoingPaymentResult(it) },
                        { paymentError.value = R.string.payment_failed }
                )

        subscriptions.add(sub)
    }

    private fun handleOutgoingPaymentResult(outgoingPaymentResult: OutgoingPaymentResult) {
        if (outgoingPaymentResult.isSuccess()) paymentSuccess.value = outgoingPaymentResult.paymentTask
        else paymentError.value = R.string.payment_failed
    }

    private fun getBalance() {
        balanceManager.refreshBalance() // Initiate a balance request to make sure the balance is updated
        val sub = balanceManager
                .balanceObservable
                .filter { it != null }
                .flatMap { it.getBalanceWithLocalBalance().toObservable() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { balance.value = it },
                        { LogUtil.w("Error while fetching balance $it") }
                )

        subscriptions.add(sub)
    }

    fun init(bundle: Bundle?) {
        this.bundle = bundle
        getPaymentTask()
    }

    private fun getPaymentTask() {
        val confirmationType = getConfirmationType()
        if (confirmationType == null) {
            LogUtil.exception("Payment confirmation type is null when trying to send a payment")
            return
        }

        try {
            findPaymentConfirmationType(confirmationType)
        } catch (e: IllegalStateException) {
            LogUtil.exception("Error when trying to find payment confirmation type", e)
        }
    }

    private fun findPaymentConfirmationType(confirmationType: Int) {
        when (confirmationType) {
            TOSHI_PAYMENT -> handleToshiPayment()
            TOSHI_PAYMENT_REQUEST -> handleToshiPaymentRequest()
            EXTERNAL -> handleExternalPayment()
            WEB -> handleWebPayment()
            TOKEN_PAYMENT -> handleTokenPayment()
            else -> LogUtil.exception("Unhandled payment")
        }
    }

    @Throws(IllegalStateException::class)
    private fun handleToshiPayment() {
        val toshiId = getToshiId()
                ?: throw IllegalStateException("toshiId is null when trying to send toshi payment")
        val encodedEthAmount = getEncodedEthAmount()
        getPaymentTaskWithToshiId(toshiId, encodedEthAmount)
    }

    @Throws(IllegalStateException::class)
    private fun handleToshiPaymentRequest() {
        val toPaymentAddress = getPaymentAddress()
                ?: throw IllegalStateException("toPaymentAddress is null when trying to send toshi payment request")
        val encodedEthAmount = getEncodedEthAmount()
        getPaymentTaskWithPaymentAddress(toPaymentAddress, encodedEthAmount)
    }

    @Throws(IllegalStateException::class)
    private fun handleExternalPayment() {
        val toPaymentAddress = getPaymentAddress()
                ?: throw IllegalStateException("toPaymentAddress is null when trying to send external payment")
        val encodedEthAmount = getEncodedEthAmount()
        getPaymentTaskWithPaymentAddress(toPaymentAddress, encodedEthAmount)
    }

    @Throws(IllegalStateException::class)
    private fun handleWebPayment() {
        val unsignedW3Transaction = getUnsignedW3Transaction()
                ?: throw IllegalStateException("unsignedW3Transaction is null when trying to send web payment")
        val callbackId = getCallbackId()
                ?: throw IllegalStateException("callbackId is null when trying to send web payment")
        getPaymentTaskWithUnsignedW3Transaction(unsignedW3Transaction, callbackId)
    }

    @Throws(IllegalStateException::class)
    private fun handleTokenPayment() {
        val toPaymentAddress = getPaymentAddress()
                ?: throw IllegalStateException("toPaymentAddress is null when trying to send token payment")
        val tokenAddress = getTokenAddress()
                ?: throw IllegalStateException("tokenAddress is null when trying to send token payment")
        val tokenSymbol = getTokenSymbol()
                ?: throw IllegalStateException("tokenSymbol is null when trying to send token payment")
        val tokenDecimals = getTokenDecimals()
                ?: throw IllegalStateException("tokenDecimals is null when trying to send token payment")
        val encodedEthAmount = getEncodedEthAmount()

        getPaymentTaskWithTokenAddress(tokenAddress, tokenSymbol, tokenDecimals, toPaymentAddress, encodedEthAmount)
    }

    private fun getToshiId(): String? = bundle?.getString(TOSHI_ID)
    private fun getUnsignedW3Transaction(): String? = bundle?.getString(UNSIGNED_TRANSACTION)
    fun getPaymentAddress(): String? = bundle?.getString(PAYMENT_ADDRESS)
    fun getTokenAddress(): String? = bundle?.getString(TOKEN_ADDRESS)
    fun getTokenSymbol(): String? = bundle?.getString(TOKEN_SYMBOL)
    fun getTokenDecimals(): Int? = bundle?.getInt(TOKEN_DECIMALS)
    fun getPaymentType(): Int? = bundle?.getInt(PAYMENT_TYPE)
    fun getEncodedEthAmount(): String = bundle?.getString(ETH_AMOUNT) ?: "0x0"
    fun getCallbackId(): String? = bundle?.getString(CALLBACK_ID)
    fun getDappUrl(): String? = bundle?.getString(DAPP_URL)
    fun getDappTitle(): String? = bundle?.getString(DAPP_TITLE)
    fun getDappFavicon(): Bitmap? = bundle?.getParcelable(DAPP_FAVICON)
    fun isSendingMaxAmount() = bundle?.getBoolean(SEND_MAX_AMOUNT, false) ?: false
    fun getConfirmationType() = bundle?.getInt(CONFIRMATION_TYPE)
    fun getCurrencyMode(): CurrencyMode {
        val currencyMode = bundle?.getSerializable(CURRENCY_MODE) ?: CurrencyMode.FIAT
        return currencyMode as CurrencyMode
    }

    private fun getPaymentTaskWithToshiId(toshiId: String, ethAmount: String) {
        val sub = Single.zip(
                getLocalPaymentAddress(),
                recipientManager.getUserFromToshiId(toshiId),
                { localPaymentAddress, recipient -> Pair(localPaymentAddress, recipient) }
        )
        .flatMap { getPaymentTask(it.first, it.second.paymentAddress, ethAmount, isSendingMaxAmount()) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { isLoading.value = true }
        .doAfterTerminate { isLoading.value = false }
        .subscribe(
                { paymentTask.value = it },
                { paymentTaskError.value = Unit }
        )

        subscriptions.add(sub)
    }

    private fun getPaymentTaskWithTokenAddress(tokenAddress: String,
                                               tokenSymbol: String,
                                               tokenDecimals: Int,
                                               toPaymentAddress: String,
                                               ethAmount: String) {
        val sub = getLocalPaymentAddress()
                .flatMap { fromPaymentAddress -> getPaymentTask(fromPaymentAddress, toPaymentAddress, ethAmount, tokenAddress, tokenSymbol, tokenDecimals) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoading.value = true }
                .doAfterTerminate { isLoading.value = false }
                .subscribe(
                        { paymentTask.value = it },
                        { paymentTaskError.value = Unit }
                )
        subscriptions.add(sub)
    }

    private fun getPaymentTaskWithPaymentAddress(toPaymentAddress: String, ethAmount: String) {
        val sub = getLocalPaymentAddress()
                .flatMap { fromPaymentAddress -> getPaymentTask(fromPaymentAddress, toPaymentAddress, ethAmount, isSendingMaxAmount()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoading.value = true }
                .doAfterTerminate { isLoading.value = false }
                .subscribe(
                        { paymentTask.value = it },
                        { paymentTaskError.value = Unit }
                )

        subscriptions.add(sub)
    }

    private fun getLocalPaymentAddress(): Single<String> {
        return toshiManager.getWallet()
                .flatMap { Single.fromCallable { it.paymentAddress } }
    }

    private fun getPaymentTaskWithUnsignedW3Transaction(unsignedW3Transaction: String, callbackId: String) {
        val transaction = SofaAdapters.get().unsignedW3TransactionFrom(unsignedW3Transaction)
        val sub = getPaymentTaskWithUnsignedW3Transaction(transaction, callbackId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoading.value = true }
                .doAfterTerminate { isLoading.value = false }
                .subscribe(
                        { paymentTask.value = it },
                        { paymentTaskError.value = Unit }
                )

        subscriptions.add(sub)
    }

    private fun getPaymentTask(fromPaymentAddress: String,
                               toPaymentAddress: String,
                               ethAmount: String,
                               sendMaxAmount: Boolean): Single<PaymentTask> {
        return transactionManager.buildPaymentTask(
                fromPaymentAddress,
                toPaymentAddress,
                ethAmount,
                sendMaxAmount
        )
    }

    private fun getPaymentTask(fromPaymentAddress: String,
                               toPaymentAddress: String,
                               ethAmount: String,
                               tokenAddress: String,
                               tokenSymbol: String,
                               tokenDecimals: Int
    ): Single<ERC20TokenPaymentTask> {
        return transactionManager.buildPaymentTask(
                fromPaymentAddress,
                toPaymentAddress,
                ethAmount,
                tokenAddress,
                tokenSymbol,
                tokenDecimals
        )
    }

    private fun getPaymentTaskWithUnsignedW3Transaction(
            unsignedW3Transaction: UnsignedW3Transaction,
            callbackId: String
    ): Single<W3PaymentTask> {
        return transactionManager.buildPaymentTask(callbackId, unsignedW3Transaction)
    }

    fun getPaymentAmount(paymentTask: PaymentTask, currencyMode: CurrencyMode): String {
        return when (currencyMode) {
            CurrencyMode.ETH -> {
                val ethAmount = EthUtil.ethAmountToUserVisibleString(paymentTask.paymentAmount.ethAmount)
                return BaseApplication.get().getString(R.string.eth_balance, ethAmount)
            }
            CurrencyMode.FIAT -> paymentTask.paymentAmount.localAmount
        }
    }

    fun getGasPrice(paymentTask: PaymentTask, currencyMode: CurrencyMode): String {
        return when (currencyMode) {
            CurrencyMode.ETH -> {
                val ethAmount = EthUtil.ethAmountToUserVisibleString(paymentTask.gasPrice.ethAmount)
                return BaseApplication.get().getString(R.string.eth_balance, ethAmount)
            }
            CurrencyMode.FIAT -> paymentTask.gasPrice.localAmount
        }
    }

    fun getTotalAmount(paymentTask: PaymentTask, currencyMode: CurrencyMode): String {
        return when (currencyMode) {
            CurrencyMode.ETH -> {
                val ethAmount = EthUtil.ethAmountToUserVisibleString(paymentTask.totalAmount.ethAmount)
                return BaseApplication.get().getString(R.string.eth_balance, ethAmount)
            }
            CurrencyMode.FIAT -> paymentTask.totalAmount.localAmount
        }
    }

    fun sendPayment(paymentTask: PaymentTask) {
        when (paymentTask) {
            is ToshiPaymentTask -> {
                transactionManager.sendPayment(paymentTask)
                isLoading. value = true
            }
            is ExternalPaymentTask -> {
                transactionManager.sendExternalPayment(paymentTask)
                isLoading. value = true
            }
            is ERC20TokenPaymentTask -> {
                transactionManager.sendERC20TokenPayment(paymentTask)
                isLoading. value = true
            }
            else -> LogUtil.w( "Invalid payment task in this context")
        }
    }

    fun finishActivityWithDelay() {
        val sub = Completable.fromAction { Completable.complete() }
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { finish.value = Unit },
                        { LogUtil.w("Error while finishing activity with delay $it") }
                )

        subscriptions.add(sub)
    }

    fun getNetworks() = Networks.getInstance()

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}