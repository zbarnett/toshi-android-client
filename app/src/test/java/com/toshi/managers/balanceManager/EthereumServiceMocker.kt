/*
 * 	Copyright (c) 2017. Toshi Inc
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

package com.toshi.managers.balanceManager

import com.toshi.manager.network.EthereumInterface
import com.toshi.manager.network.EthereumServiceInterface
import com.toshi.model.network.GcmDeregistration
import com.toshi.model.network.GcmRegistration
import com.toshi.model.network.SentTransaction
import com.toshi.model.network.ServerTime
import com.toshi.model.network.SignedTransaction
import com.toshi.model.network.TransactionRequest
import com.toshi.model.network.UnsignedTransaction
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import rx.Completable
import rx.Single

class EthereumServiceMocker {
    fun mock(testTokenBuilder: TestTokenBuilder = TestTokenBuilder()): EthereumServiceInterface {
        val ethApi = Mockito.mock(EthereumInterface::class.java)

        Mockito.`when`(ethApi.getTokens(anyString()))
                .thenReturn(Single.just(testTokenBuilder.createERC20TokenList()))
        Mockito.`when`(ethApi.getToken(anyString(), anyString()))
                .thenReturn(Single.just(testTokenBuilder.createERC20Token()))
        Mockito.`when`(ethApi.getCollectibles(anyString()))
                .thenReturn(Single.just(testTokenBuilder.createERC721TokenList()))
        Mockito.`when`(ethApi.getCollectible(anyString(), anyString()))
                .thenReturn(Single.just(testTokenBuilder.createERC721Token()))

        Mockito.`when`(ethApi.timestamp)
                .thenReturn(Single.just(ServerTime(1L)))
        Mockito.`when`(ethApi.registerGcm(any(Long::class.java), any(GcmRegistration::class.java)))
                .thenReturn(Completable.complete())
        Mockito.`when`(ethApi.unregisterGcm(any(Long::class.java), any(GcmDeregistration::class.java)))
                .thenReturn(Completable.complete())

        Mockito.`when`(ethApi.sendSignedTransaction(any(Long::class.java), any(SignedTransaction::class.java)))
                .thenReturn(Single.just(SentTransaction()))

        val ethService = Mockito.mock(EthereumServiceInterface::class.java)

        Mockito.`when`(ethService.get())
                .thenReturn(ethApi)

        return ethService
    }

    fun mockWithErrorResponse(): EthereumServiceInterface {
        val ethApi = Mockito.mock(EthereumInterface::class.java)

        Mockito.`when`(ethApi.timestamp)
                .thenThrow(IllegalStateException("No network"))
        Mockito.`when`(ethApi.registerGcm(any(Long::class.java), any(GcmRegistration::class.java)))
                .thenThrow(IllegalStateException("No network"))
        Mockito.`when`(ethApi.unregisterGcm(any(Long::class.java), any(GcmDeregistration::class.java)))
                .thenThrow(IllegalStateException("No network"))

        val ethService = Mockito.mock(EthereumServiceInterface::class.java)

        Mockito.`when`(ethService.get())
                .thenReturn(ethApi)

        return ethService
    }

    fun mockCreateTransaction(unsignedTransaction: UnsignedTransaction): EthereumServiceInterface {
        val ethApi = Mockito.mock(EthereumInterface::class.java)

        Mockito
                .`when`(ethApi.createTransaction(any(TransactionRequest::class.java)))
                .thenReturn(Single.just(unsignedTransaction))

        val ethService = Mockito.mock(EthereumServiceInterface::class.java)

        Mockito.`when`(ethService.get())
                .thenReturn(ethApi)

        return ethService
    }
}