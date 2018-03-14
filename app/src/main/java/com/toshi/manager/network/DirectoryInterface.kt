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

package com.toshi.manager.network

import com.toshi.model.network.dapp.DappResult
import com.toshi.model.network.dapp.DappSearchResult
import com.toshi.model.network.dapp.DappSections
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Single

interface DirectoryInterface {
    @GET("v1/dapps/frontpage")
    fun getFrontpageDapps(): Single<DappSections>

    @GET("v1/dapps/")
    fun search(@Query("query") query: String): Single<DappSearchResult>

    @GET("v1/dapps/?limit=500")
    fun getAllDapps(): Single<DappSearchResult>

    @GET("v1/dapps/")
    fun getAllDappsWithOffset(
            @Query("offset") offset: Int,
            @Query("limit") limit: Int = 20
    ): Single<DappSearchResult>

    @GET("v1/dapps/")
    fun getAllDappsInCategory(
            @Query("category") categoryId: Int,
            @Query("offset") offset: Int,
            @Query("limit") limit: Int
    ): Single<DappSearchResult>

    @GET("v1/dapp/{dapp_id}")
    fun getDapp(@Path("dapp_id") dappId: Long): Single<DappResult>
}