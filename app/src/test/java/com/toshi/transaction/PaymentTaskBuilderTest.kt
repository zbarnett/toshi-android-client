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

package com.toshi.transaction

import com.toshi.crypto.util.TypeConverter
import com.toshi.manager.model.ERC20TokenPaymentTask
import com.toshi.manager.model.PaymentTask
import com.toshi.manager.model.ToshiPaymentTask
import com.toshi.manager.model.W3PaymentTask
import com.toshi.model.local.UnsignedW3Transaction
import com.toshi.util.EthUtil
import com.toshi.util.paymentTask.PaymentTaskBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.text.DecimalFormat

class PaymentTaskBuilderTest {

    private lateinit var paymentTaskBuilder: PaymentTaskBuilder
    private lateinit var unsignedW3Transaction: UnsignedW3Transaction

    private val paymentTaskBuilderMocker by lazy { PaymentTaskBuilderMocker() }

    @Before
    fun setup() {
        unsignedW3Transaction = paymentTaskBuilderMocker.mockUnsignedW3Transaction()
        paymentTaskBuilder = paymentTaskBuilderMocker.mockPaymentTaskBuilder()
    }

    @Test
    fun testBuildToshiPaymentTask() {
        val paymentTask = paymentTaskBuilder.buildPaymentTask(
                fromPaymentAddress = paymentTaskBuilderMocker.testSenderPaymentAddress,
                toPaymentAddress = paymentTaskBuilderMocker.testReceiverPaymentAddress,
                ethAmount = paymentTaskBuilderMocker.testEthAmountHex,
                sendMaxAmount = false
        ).toBlocking().value()

        assertThat(paymentTask, instanceOf(ToshiPaymentTask::class.java))
        paymentTask as ToshiPaymentTask
        assertThat(paymentTask.user, `is`(notNullValue()))
        assertPaymentTaskValues(paymentTask)
    }

    @Test
    fun testBuildW3PaymentTask() {
        val paymentTask = paymentTaskBuilder
                .buildW3PaymentTask(paymentTaskBuilderMocker.callbackId, unsignedW3Transaction)
                .toBlocking()
                .value()

        assertThat(paymentTask, instanceOf(W3PaymentTask::class.java))
        assertThat(paymentTask.callbackId, `is`(notNullValue()))
        assertPaymentTaskValues(paymentTask)
    }

    private fun assertPaymentTaskValues(paymentTask: PaymentTask) {
        assertThat(paymentTask.payment.toAddress, `is`(paymentTaskBuilderMocker.testReceiverPaymentAddress))
        assertThat(paymentTask.payment.fromAddress, `is`(paymentTaskBuilderMocker.testSenderPaymentAddress))
        assertThat(paymentTask.payment.value, `is`(paymentTaskBuilderMocker.testEthAmountHex))
        val expectedEthAmount = EthUtil.weiToEth(TypeConverter.StringHexToBigInteger(paymentTaskBuilderMocker.testEthAmountHex))
        assertThat(paymentTask.paymentAmount.ethAmount, `is`(expectedEthAmount))
        assertThat(paymentTask.totalAmount.ethAmount, `is`(expectedEthAmount + paymentTask.gasPrice.ethAmount))
    }

    @Test
    fun testBuildERC20PaymentTask() {
        val paymentTask = paymentTaskBuilder.buildERC20PaymentTask(
                fromPaymentAddress = paymentTaskBuilderMocker.testSenderPaymentAddress,
                toPaymentAddress = paymentTaskBuilderMocker.testReceiverPaymentAddress,
                value = paymentTaskBuilderMocker.testTokenAmount,
                tokenAddress = paymentTaskBuilderMocker.testTokenAddress,
                tokenSymbol = paymentTaskBuilderMocker.tokenSymbol,
                tokenDecimals = paymentTaskBuilderMocker.tokenDecimals
        ).toBlocking().value()

        assertThat(paymentTask, instanceOf(ERC20TokenPaymentTask::class.java))
        assertThat(paymentTask.tokenSymbol, `is`(paymentTaskBuilderMocker.tokenSymbol))
        assertThat(paymentTask.payment.toAddress, `is`(paymentTaskBuilderMocker.testReceiverPaymentAddress))
        assertThat(paymentTask.payment.fromAddress, `is`(paymentTaskBuilderMocker.testSenderPaymentAddress))
        assertThat(paymentTask.gasPrice.ethAmount, `is`(notNullValue()))
        assertTokenValue(paymentTask)
    }

    private fun assertTokenValue(paymentTask: ERC20TokenPaymentTask) {
        val expectedTokenValue = BigDecimal(paymentTaskBuilderMocker.testTokenAmount)
        val actualTokenValue = BigDecimal(paymentTask.tokenValue)
        val decimalFormat = DecimalFormat("#.00")
        assertThat(decimalFormat.format(expectedTokenValue), `is`(decimalFormat.format(actualTokenValue)))
        val actualTokenHexValue = TypeConverter.formatHexString(paymentTask.payment.value, paymentTaskBuilderMocker.tokenDecimals, EthUtil.ETH_FORMAT)
        assertThat(decimalFormat.format(BigDecimal(actualTokenHexValue)), `is`(decimalFormat.format(BigDecimal(paymentTaskBuilderMocker.testTokenAmount))))
    }
}