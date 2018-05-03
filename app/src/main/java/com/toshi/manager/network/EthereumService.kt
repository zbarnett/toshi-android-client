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

package com.toshi.manager.network

import com.squareup.moshi.Moshi
import com.toshi.manager.network.interceptor.AppInfoUserAgentInterceptor
import com.toshi.manager.network.interceptor.LoggingInterceptor
import com.toshi.manager.network.interceptor.SigningInterceptor
import com.toshi.model.adapter.BigIntegerAdapter
import com.toshi.model.local.network.Networks
import com.toshi.model.sofa.SofaAdapters
import com.toshi.model.sofa.SofaMessage
import com.toshi.model.sofa.payment.Payment
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import rx.Single
import rx.schedulers.Schedulers

object EthereumService : EthereumServiceInterface {

    private var ethereumInterface: EthereumInterface
    private var baseUrl: String

    override fun get() = ethereumInterface

    init {
        baseUrl = getBaseUrl()
        ethereumInterface = buildEthereumInterface(baseUrl)
    }

    private fun getBaseUrl(): String {
        val network = Networks.getInstance().currentNetwork
        return network.url
    }

    private fun buildEthereumInterface(baseUrl: String): EthereumInterface {
        val moshi = Moshi.Builder()
                .add(BigIntegerAdapter())
                .build()

        val rxAdapter = RxJavaCallAdapterFactory
                .createWithScheduler(Schedulers.io())

        val client: OkHttpClient.Builder = OkHttpClient.Builder()
                .addInterceptor(AppInfoUserAgentInterceptor())
                .addInterceptor(SigningInterceptor())
                .addInterceptor(buildLoggingInterceptor())

        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(rxAdapter)
                .client(client.build())
                .build()

        return retrofit.create(EthereumInterface::class.java)
    }

    private fun buildLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(LoggingInterceptor())
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    override fun changeBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
        ethereumInterface = buildEthereumInterface(this.baseUrl)
    }

    override fun getStatusOfTransaction(transactionHash: String): Single<Payment> {
        return Single.fromCallable {
            val networkUrl = Networks.getInstance().defaultNetwork.url
            val url = "$networkUrl/v1/tx/$transactionHash?format=sofa"

            val request = Request.Builder()
                    .url(url)
                    .build()

            val response = OkHttpClient()
                    .newCall(request)
                    .execute()

            if (response.code() == 404) return@fromCallable null
            val sofaMessage = SofaMessage().makeNew(response.body()?.string().orEmpty())
            response.close()

            return@fromCallable SofaAdapters.get().paymentFrom(sofaMessage.payload)
        }
    }
}