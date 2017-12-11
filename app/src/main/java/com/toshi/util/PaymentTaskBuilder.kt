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
        return createUnsignedTransaction(payment)
                .flatMap { unsignedTransaction ->
                    calculateGasPrice(unsignedTransaction)
                            .map { gasPrice -> PaymentTask(payment, unsignedTransaction, gasPrice) }
                }
                .flatMap { paymentTask -> recipientManager.getUserFromPaymentAddress(toPaymentAddress)
                        .map { user -> paymentTask.setUser(user) } }
    }

    fun buildPaymentTask(unsignedW3Transaction: UnsignedW3Transaction): Single<PaymentTask> {
        return createUnsignedW3Transaction(unsignedW3Transaction)
                .flatMap { unsignedTransaction ->
                    calculateGasPrice(unsignedTransaction)
                            .map { gasPrice -> PaymentTask(unsignedTransaction, gasPrice) }
                }
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