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

package com.toshi.util.paymentTask

import com.toshi.crypto.util.TypeConverter
import com.toshi.manager.model.PaymentTask
import com.toshi.manager.model.PaymentTask.Builder
import com.toshi.manager.network.EthereumService
import com.toshi.model.local.EthAndFiat
import com.toshi.model.local.UnsignedW3Transaction
import com.toshi.model.network.ExchangeRate
import com.toshi.model.network.UnsignedTransaction
import com.toshi.model.sofa.Payment
import com.toshi.util.EthUtil
import com.toshi.view.BaseApplication
import rx.Single
import java.math.BigDecimal

class PaymentTaskBuilder {

    private val balanceManager by lazy { BaseApplication.get().balanceManager }
    private val recipientManager by lazy { BaseApplication.get().recipientManager }
    private val transactionBuilder by lazy { TransactionRequestBuilder() }

    fun buildPaymentTask(fromPaymentAddress: String,
                         toPaymentAddress: String,
                         ethAmount: String): Single<PaymentTask> {
        val payment = Payment()
                .setValue(ethAmount)
                .setFromAddress(fromPaymentAddress)
                .setToAddress(toPaymentAddress)

        val paymentTaskBuilder = Builder()
                .setPayment(payment)

        return createUnsignedTransaction(payment)
                .flatMap { addPaymentInfoToPaymentTask(paymentTaskBuilder, it) }
                .flatMap { addUserToPaymentTask(paymentTaskBuilder, toPaymentAddress) }
                .map { it.build() }
    }

    private fun addUserToPaymentTask(builder: Builder, toPaymentAddress: String): Single<Builder> {
        return recipientManager.getUserFromPaymentAddress(toPaymentAddress)
                .onErrorReturn { null }
                .map { user -> builder.setUser(user) }
    }

    fun buildPaymentTask(callbackId: String, unsignedW3Transaction: UnsignedW3Transaction): Single<PaymentTask> {
        val payment = Payment()
                .setValue(unsignedW3Transaction.value)
                .setFromAddress(unsignedW3Transaction.from)
                .setToAddress(unsignedW3Transaction.to)

        val paymentTaskBuilder = Builder()
                .setCallbackId(callbackId)
                .setPayment(payment)

        return createUnsignedW3Transaction(unsignedW3Transaction)
                .flatMap { addPaymentInfoToPaymentTask(paymentTaskBuilder, it) }
                .map { it.build() }
    }

    private fun createUnsignedW3Transaction(unsignedW3Transaction: UnsignedW3Transaction): Single<UnsignedTransaction> {
        val transactionRequest = transactionBuilder.generateTransactionRequest(unsignedW3Transaction)
        return EthereumService
                .getApi()
                .createTransaction(transactionRequest)
    }

    private fun addPaymentInfoToPaymentTask(builder: Builder, unsignedTransaction: UnsignedTransaction): Single<Builder> {
        return calculatePaymentInfo(unsignedTransaction)
                .map { builder
                        .setUnsignedTransaction(unsignedTransaction)
                        .setPaymentAmount(it.paymentAmount)
                        .setGasPrice(it.gasAmount)
                        .setTotalAmount(it.totalAmount)
                }
    }

    private fun calculatePaymentInfo(unsignedTransaction: UnsignedTransaction): Single<PaymentTaskInfo> {
        val sendEthAmount = getSendEthAmount(unsignedTransaction)
        val gasEthAmount = getGasEthAmount(unsignedTransaction)
        val totalEthAmount = BigDecimal(0).add(sendEthAmount).add(gasEthAmount)

        return balanceManager.getLocalCurrencyExchangeRate()
                .map { mapPaymentValuesToFiat(it, sendEthAmount, gasEthAmount, totalEthAmount) }
    }

    private fun getGasEthAmount(unsignedTransaction: UnsignedTransaction): BigDecimal {
        val gas = TypeConverter.StringHexToBigInteger(unsignedTransaction.gas)
        val gasPrice = TypeConverter.StringHexToBigInteger(unsignedTransaction.gasPrice)
        return EthUtil.weiToEth(gasPrice.multiply(gas))
    }

    private fun getSendEthAmount(unsignedTransaction: UnsignedTransaction): BigDecimal {
        val weiAmount = TypeConverter.StringHexToBigInteger(unsignedTransaction.value)
        return EthUtil.weiToEth(weiAmount)
    }

    private fun mapPaymentValuesToFiat(exchangeRate: ExchangeRate,
                                       sendEthAmount: BigDecimal,
                                       gasEthAmount: BigDecimal,
                                       totalEthAmount: BigDecimal): PaymentTaskInfo {
        val sendLocalAmount = balanceManager.toLocalCurrencyString(exchangeRate, sendEthAmount)
        val sendAmount = EthAndFiat(sendEthAmount, sendLocalAmount)
        val gasLocalAmount = balanceManager.toLocalCurrencyString(exchangeRate, gasEthAmount)
        val gasAmount = EthAndFiat(gasEthAmount, gasLocalAmount)
        val totalLocalAmount = balanceManager.toLocalCurrencyString(exchangeRate, totalEthAmount)
        val totalAmount = EthAndFiat(totalEthAmount, totalLocalAmount)
        return PaymentTaskInfo(sendAmount, gasAmount, totalAmount)
    }

    private fun createUnsignedTransaction(payment: Payment): Single<UnsignedTransaction> {
        val transactionRequest = transactionBuilder.generateTransactionRequest(payment)
        return EthereumService
                .getApi()
                .createTransaction(transactionRequest)
    }
}

private data class PaymentTaskInfo(
        val paymentAmount: EthAndFiat,
        val gasAmount: EthAndFiat,
        val totalAmount: EthAndFiat
)