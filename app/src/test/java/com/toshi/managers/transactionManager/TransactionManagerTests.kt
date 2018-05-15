/*
 *
 *  * 	Copyright (c) 2018. Toshi Inc
 *  *
 *  * 	This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License, or
 *  *     (at your option) any later version.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.toshi.managers.transactionManager

import com.toshi.manager.TransactionManager
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class TransactionManagerTests {

    private lateinit var transactionManager: TransactionManager
    private val transactionManagerMocker by lazy { TransactionManagerMocker() }

    @Before
    fun before() {
        initTransactionManagerWithoutWallet()
    }

    @Test
    fun `sign transaction when wallet is null`() {
        initTransactionManagerWithoutWallet()
        val w3PaymentTask = W3PaymentTaskBuilder().createW3PaymentTask()
        try {
            transactionManager.signW3Transaction(w3PaymentTask).toBlocking().value()
        } catch (e: IllegalStateException) {
            return
        }
        fail()
    }

    @Test
    fun `sign transaction when wallet is not null`() {
        initTransactionManagerWithWallet()
        val w3PaymentTask = W3PaymentTaskBuilder().createW3PaymentTask()
        val signedW3Transaction = transactionManager.signW3Transaction(w3PaymentTask).toBlocking().value()
        assertNotNull(signedW3Transaction)
    }

    private fun initTransactionManagerWithoutWallet() {
        transactionManager = transactionManagerMocker.initTransactionManagerWithoutWallet()
    }

    private fun initTransactionManagerWithWallet() {
        transactionManager = transactionManagerMocker.initTransactionManagerWithWallet()
    }
}