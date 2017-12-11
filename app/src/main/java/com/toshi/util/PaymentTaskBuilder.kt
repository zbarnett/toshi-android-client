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

package com.toshi.util

import com.toshi.crypto.util.TypeConverter
import com.toshi.manager.model.PaymentTask
import com.toshi.manager.model.PaymentTask.Builder
import com.toshi.manager.network.EthereumService
import com.toshi.model.local.GasPrice
import com.toshi.model.local.UnsignedW3Transaction
import com.toshi.model.network.TransactionRequest
import com.toshi.model.network.UnsignedTransaction
import com.toshi.model.sofa.Payment
import com.toshi.view.BaseApplication
import rx.Single

class PaymentTaskBuilder {

    private val balanceManager by lazy { BaseApplication.get().balanceManager }
    private val recipientManager by lazy { BaseApplication.get().recipientManager }

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
                .flatMap { addGasPriceToPaymentTask(paymentTaskBuilder, it) }
                .flatMap { addUserToPaymentTask(paymentTaskBuilder, toPaymentAddress) }
                .map { it.build() }
    }

    private fun addUserToPaymentTask(builder: Builder, toPaymentAddress: String): Single<Builder> {
        return recipientManager.getUserFromPaymentAddress(toPaymentAddress)
                .map { user -> builder.setUser(user) }
    }

    fun buildPaymentTask(unsignedW3Transaction: UnsignedW3Transaction): Single<PaymentTask> {
        val paymentTaskBuilder = Builder()
        return createUnsignedW3Transaction(unsignedW3Transaction)
                .flatMap { addGasPriceToPaymentTask(paymentTaskBuilder, it) }
                .map { it.build() }
    }

    private fun addGasPriceToPaymentTask(builder: Builder, unsignedTransaction: UnsignedTransaction): Single<Builder> {
        return calculateGasPrice(unsignedTransaction)
                .map { builder
                        .setUnsignedTransaction(unsignedTransaction)
                        .setGasPrice(it) }
    }

    private fun createUnsignedW3Transaction(transaction: UnsignedW3Transaction): Single<UnsignedTransaction> {
        val transactionRequest = generateTransactionRequest(transaction)
        return EthereumService
                .getApi()
                .createTransaction(transactionRequest)
    }

    private fun calculateGasPrice(unsignedTransaction: UnsignedTransaction): Single<GasPrice> {
        val gas = TypeConverter.StringHexToBigInteger(unsignedTransaction.gas)
        val gasPrice = TypeConverter.StringHexToBigInteger(unsignedTransaction.gasPrice)
        val gasEthAmount = EthUtil.weiToEth(gasPrice.multiply(gas))
        return balanceManager.convertEthToLocalCurrencyString(gasEthAmount)
                .map { gasLocalAmount -> GasPrice(gasEthAmount, gasLocalAmount); }
    }

    private fun createUnsignedTransaction(payment: Payment): Single<UnsignedTransaction> {
        val transactionRequest = generateTransactionRequest(payment)
        return EthereumService
                .getApi()
                .createTransaction(transactionRequest)
    }

    private fun generateTransactionRequest(payment: Payment): TransactionRequest {
        return TransactionRequest()
                .setValue(payment.value)
                .setFromAddress(payment.fromAddress)
                .setToAddress(payment.toAddress)
    }

    private fun generateTransactionRequest(transaction: UnsignedW3Transaction): TransactionRequest {
        return TransactionRequest()
                .setValue(transaction.value)
                .setFromAddress(transaction.from)
                .setToAddress(transaction.to)
                .setData(transaction.data)
                .setGas(transaction.gas)
                .setGasPrice(transaction.gasPrice)
    }
}