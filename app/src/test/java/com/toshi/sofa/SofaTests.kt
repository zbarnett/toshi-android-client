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

package com.toshi.sofa

import com.toshi.model.sofa.SofaAdapters
import com.toshi.model.sofa.SofaMessage
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class SofaTests {

    private lateinit var sofaAdapter: SofaAdapters

    @Before
    fun setup() {
        sofaAdapter = SofaAdapters.get()
    }

    @Test
    fun testParsingTokenPayment() {
        val rawTokenPayment = "SOFA::TokenPayment:{" +
                "\"txHash\": \"0xfec1be6e81de2130392365075ce7703cb07e7642a9d9630204d6517391066551\"," +
                "\"fromAddress\": \"0x4a40d412f25db163a9af6190752c0758bdca6aa3\"," +
                "\"toAddress\": \"0xc7dfa34e214de6a7c206b61c37765498c1f96fbc\"," +
                "\"status\": \"unconfirmed\"," +
                "\"value\": \"0x1\"," +
                "\"contractAddress\":\"0xd47d8dd7fd38c36cf5f1031602dc8958ac9953da\"" +
                "}"
        val sofaMessage = SofaMessage().makeNew(rawTokenPayment)
        val tokenPayment = sofaAdapter.tokenPaymentFrom(sofaMessage.payload)
        assertThat(tokenPayment.txHash, `is`("0xfec1be6e81de2130392365075ce7703cb07e7642a9d9630204d6517391066551"))
        assertThat(tokenPayment.fromAddress, `is`("0x4a40d412f25db163a9af6190752c0758bdca6aa3"))
        assertThat(tokenPayment.toAddress, `is`("0xc7dfa34e214de6a7c206b61c37765498c1f96fbc"))
        assertThat(tokenPayment.status, `is`("unconfirmed"))
        assertThat(tokenPayment.value, `is`("0x1"))
        assertThat(tokenPayment.contractAddress, `is`("0xd47d8dd7fd38c36cf5f1031602dc8958ac9953da"))
    }

    @Test
    fun testParsingPayment() {
        val rawPayment = "SOFA::Payment:{" +
                "\"status\": \"unconfirmed\"," +
                "\"txHash\": \"0x7696626ec165067dbfe6af63ba1d70ece606da86cad928c5e2f0cc6bab6f0452\"," +
                "\"value\": \"0x6d23ad5f8000\"," +
                "\"fromAddress\":" + "\"0x4a40d412f25db163a9af6190752c0758bdca6aa3\"," +
                "\"toAddress\": \"0xc7dfa34e214de6a7c206b61c37765498c1f96fbc\"" +
                "}"
        val sofaMessage = SofaMessage().makeNew(rawPayment)
        val payment = sofaAdapter.paymentFrom(sofaMessage.payload)
        assertThat(payment.status, `is`("unconfirmed"))
        assertThat(payment.txHash, `is`("0x7696626ec165067dbfe6af63ba1d70ece606da86cad928c5e2f0cc6bab6f0452"))
        assertThat(payment.value, `is`("0x6d23ad5f8000"))
        assertThat(payment.fromAddress, `is`("0x4a40d412f25db163a9af6190752c0758bdca6aa3"))
        assertThat(payment.toAddress, `is`("0xc7dfa34e214de6a7c206b61c37765498c1f96fbc"))
    }
}