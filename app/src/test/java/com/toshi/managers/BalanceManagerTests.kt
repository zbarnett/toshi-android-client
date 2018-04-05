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

package com.toshi.managers

import com.toshi.manager.BalanceManager
import com.toshi.model.network.ExchangeRate
import com.toshi.model.sofa.payment.Payment
import com.toshi.util.CurrencyUtil
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class BalanceManagerTests {

    private val fromCurrency = "ETH"
    private val localCurrency = "USD"
    private val testSenderPaymentAddress = "0x4a40d412f25db163a9af6190752c0758bdca6aa3"
    private val testReceiverPaymentAddress = "0x4a40d412f25db163a9af6190752c0758bdca6aa0"
    private val testSendValue = "0x1bc16d674ec80000"
    private val testLocalValue = "1162.20"
    private val marketRate = "581.10"
    private val balance = "0x0"

    private lateinit var exchangeRate: ExchangeRate
    private lateinit var payment: Payment
    private lateinit var balanceManager: BalanceManager
    private val testTokenBuilder by lazy { TestTokenBuilder() }

    @Before
    fun setup() {
        createExchangeRate()
        createPayment()
        mockBalanceManager()
    }

    private fun createExchangeRate() {
        exchangeRate = ExchangeRate(fromCurrency, localCurrency, BigDecimal(marketRate), 0)
    }

    private fun createPayment() {
        payment = Payment()
                .setValue(testSendValue)
                .setFromAddress(testSenderPaymentAddress)
                .setToAddress(testReceiverPaymentAddress)
    }

    private fun mockBalanceManager() {
        val balanceManagerMocker = BalanceManagerMocker(
                exchangeRate = exchangeRate,
                testTokenBuilder = testTokenBuilder,
                lastKnownBalance = balance,
                localCurrency = localCurrency
        )
        balanceManager = balanceManagerMocker.mock()
    }

    @Test
    fun testGetERC20Tokens() {
        val erc20Tokens = balanceManager
                .getERC20Tokens()
                .toBlocking()
                .value()

        val expectedTokenList = testTokenBuilder.createERC20TokenList()
        assertThat(erc20Tokens, `is`(expectedTokenList))
    }

    @Test
    fun testGetERC20Token() {
        val erc20Token = balanceManager
                .getERC20Token(testTokenBuilder.contractAddress)
                .toBlocking()
                .value()

        val expectedERC20Token = testTokenBuilder.createERC20Token()
        assertThat(erc20Token, `is`(expectedERC20Token))
    }

    @Test
    fun testGetERC721Tokens() {
        val erc721Tokens = balanceManager
                .getERC721Tokens()
                .toBlocking()
                .value()

        val expectedERC721Tokens = testTokenBuilder.createERC721TokenList()
        assertThat(erc721Tokens, `is`(expectedERC721Tokens))
    }

    @Test
    fun testGetERC721Token() {
        val erc721Token = balanceManager
                .getERC721Token(testTokenBuilder.contractAddress)
                .toBlocking()
                .value()

        val expectedERC721Token = testTokenBuilder.createERC721Token()
        assertThat(erc721Token, `is`(expectedERC721Token))
    }

    @Test
    fun testGenerateLocalPrice() {
        val paymentWithLocalBalance = balanceManager
                .generateLocalPrice(payment)
                .toBlocking()
                .value()

        assertThat(paymentWithLocalBalance.fromAddress, `is`("0x4a40d412f25db163a9af6190752c0758bdca6aa3"))
        assertThat(paymentWithLocalBalance.toAddress, `is`("0x4a40d412f25db163a9af6190752c0758bdca6aa0"))
        assertThat(paymentWithLocalBalance.value, `is`("0x1bc16d674ec80000"))

        val expectedLocalValue = CurrencyUtil.getNumberFormat().format(BigDecimal(testLocalValue))
        val currencyCode = CurrencyUtil.getCode(exchangeRate.to)
        val currencySymbol = CurrencyUtil.getSymbol(exchangeRate.to)
        assertThat(paymentWithLocalBalance.localPrice, `is`("$currencySymbol$expectedLocalValue $currencyCode"))
    }

    @Test
    fun testConvertLocalCurrencyToEth() {
        val localValue = BigDecimal(testLocalValue)
        val ethValue = balanceManager
                .convertLocalCurrencyToEth(localValue)
                .toBlocking()
                .value()

        val expectedValue = BigDecimal("2").stripTrailingZeros()
        val actualValue = ethValue.stripTrailingZeros()

        assertThat(actualValue, `is`(expectedValue))
    }

    @Test
    fun testBalanceObservable() {
        val balance = balanceManager
                .balanceObservable
                .toBlocking()
                .first()
        assertThat(balance.unconfirmedBalanceAsHex, `is`("0x0"))
    }
}