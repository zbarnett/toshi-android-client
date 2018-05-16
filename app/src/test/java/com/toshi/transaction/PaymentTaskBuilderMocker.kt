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

package com.toshi.transaction

import com.toshi.crypto.HDWallet
import com.toshi.crypto.util.TypeConverter
import com.toshi.extensions.createSafeBigDecimal
import com.toshi.manager.BalanceManager
import com.toshi.manager.RecipientManager
import com.toshi.manager.TransactionManager
import com.toshi.manager.network.EthereumServiceInterface
import com.toshi.managers.balanceManager.BalanceManagerMocker
import com.toshi.managers.balanceManager.EthereumServiceMocker
import com.toshi.masterSeed
import com.toshi.mockWallet
import com.toshi.mockWalletSubject
import com.toshi.model.local.UnsignedW3Transaction
import com.toshi.model.local.User
import com.toshi.model.network.ExchangeRate
import com.toshi.model.network.UnsignedTransaction
import com.toshi.util.EthUtil
import com.toshi.util.paymentTask.PaymentTaskBuilder
import org.mockito.Mockito
import rx.Observable
import rx.Single
import rx.schedulers.Schedulers
import java.math.BigDecimal

class PaymentTaskBuilderMocker {

    lateinit var exchangeRate: ExchangeRate
    lateinit var testEthAmountHex: String

    val testFiatAmount = "10.00"
    val testTokenAmount = "15.00"
    val testSenderPaymentAddress = "0x4a40d412f25db163a9af6190752c0758bdca6aa3"
    val testReceiverPaymentAddress = "0x4a40d412f25db163a9af6190752c0758bdca6aa0"
    val testTokenAddress = "0x4a40d412f25db163a9af6190752c0758bdca6aa1"
    val fromCurrency = "ETH"
    val toCurrency = "USD"
    val tokenDecimals = 18
    val callbackId = "1"
    val tokenSymbol = "OMG"

    init {
        mockEthToUsdExchangeRate()
        calcSendAmount()
    }

    private fun mockEthToUsdExchangeRate() {
        exchangeRate = ExchangeRate(fromCurrency, toCurrency, BigDecimal("581.10"), 0)
    }

    private fun calcSendAmount() {
        val decimalString = EthUtil.fiatToEth(exchangeRate, createSafeBigDecimal(testFiatAmount))
        val weiAmount = EthUtil.ethToWei(BigDecimal(decimalString))
        testEthAmountHex = TypeConverter.toJsonHex(weiAmount)
    }

    fun mockUnsignedW3Transaction(): UnsignedW3Transaction {
        val unsignedW3Transaction = Mockito.mock(UnsignedW3Transaction::class.java)
        Mockito
                .`when`(unsignedW3Transaction.from)
                .thenReturn(testSenderPaymentAddress)

        Mockito
                .`when`(unsignedW3Transaction.to)
                .thenReturn(testReceiverPaymentAddress)

        Mockito
                .`when`(unsignedW3Transaction.value)
                .thenReturn(testEthAmountHex)

        return unsignedW3Transaction
    }

    fun mockPaymentTaskBuilder(): PaymentTaskBuilder {
        val unsignedTransaction = mockUnsignedTransaction()
        return PaymentTaskBuilder(
                transactionManager = mockTransactionManager(unsignedTransaction),
                balanceManager = mockBalanceManager(),
                recipientManager = mockRecipientManager()
        )
    }

    private fun mockUnsignedTransaction(): UnsignedTransaction {
        val unsignedTransaction = Mockito.mock(UnsignedTransaction::class.java)
        Mockito
                .`when`(unsignedTransaction.gas)
                .thenReturn("0x5208")
        Mockito
                .`when`(unsignedTransaction.gasPrice)
                .thenReturn("0x3b9aca00")
        Mockito
                .`when`(unsignedTransaction.nonce)
                .thenReturn("0x746f6b65e2")
        Mockito
                .`when`(unsignedTransaction.transaction)
                .thenReturn("0xef85746f6b65e2843b9aca00825208944a40d412f25db163a9af6190752c0758bdca6aa387061d3d89a8900080748080")
        Mockito
                .`when`(unsignedTransaction.value)
                .thenReturn(testEthAmountHex)

        return unsignedTransaction
    }

    private fun mockTransactionManager(unsignedTransaction: UnsignedTransaction): TransactionManager {
        return TransactionManager(
                ethService = mockEthereumService(unsignedTransaction),
                walletObservable = mockWalletObservable(),
                scheduler = Schedulers.trampoline()
        )
    }

    private fun mockEthereumService(unsignedTransaction: UnsignedTransaction): EthereumServiceInterface {
        return EthereumServiceMocker().mockCreateTransaction(unsignedTransaction)
    }

    private fun mockWalletObservable(): Observable<HDWallet> {
        val wallet = mockWallet(masterSeed)
        return mockWalletSubject(wallet)
    }

    private fun mockRecipientManager(): RecipientManager {
        val recipientManager = Mockito.mock(RecipientManager::class.java)
        Mockito
                .`when`(recipientManager.getUserFromPaymentAddress(testReceiverPaymentAddress))
                .thenReturn(Single.just(User()))
        return recipientManager
    }

    private fun mockBalanceManager(): BalanceManager {
        return BalanceManagerMocker(exchangeRate = exchangeRate).mockWithWalletInit()
    }
}