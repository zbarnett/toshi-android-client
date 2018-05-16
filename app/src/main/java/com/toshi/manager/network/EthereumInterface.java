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

package com.toshi.manager.network;

import com.toshi.model.network.Balance;
import com.toshi.model.network.GcmDeregistration;
import com.toshi.model.network.GcmRegistration;
import com.toshi.model.network.SentTransaction;
import com.toshi.model.network.ServerTime;
import com.toshi.model.network.SignedTransaction;
import com.toshi.model.network.TransactionRequest;
import com.toshi.model.network.UnsignedTransaction;
import com.toshi.model.network.token.CustomERCToken;
import com.toshi.model.network.token.ERC20Token;
import com.toshi.model.network.token.ERC20Tokens;
import com.toshi.model.network.token.ERC721TokenWrapper;
import com.toshi.model.network.token.ERC721Tokens;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Completable;
import rx.Single;

public interface EthereumInterface {

    @POST("/v1/tx/skel")
    Single<UnsignedTransaction> createTransaction(@Body TransactionRequest request);

    @POST("/v1/tx")
    Single<SentTransaction> sendSignedTransaction(
            @Query("timestamp") long timestamp,
            @Body SignedTransaction transaction);


    @GET("/v1/balance/{id}")
    Single<Balance> getBalance(@Path("id") String walletAddress);

    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @POST("/v1/gcm/register")
    Completable registerGcm(
            @Query("timestamp") long timestamp,
            @Body GcmRegistration gcmRegistration
    );

    @POST("/v1/gcm/deregister")
    Completable unregisterGcm(
            @Query("timestamp") long timestamp,
            @Body GcmDeregistration gcmDeregistration
    );

    @GET("/v1/tokens/{wallet_address}")
    Single<ERC20Tokens> getTokens(@Path("wallet_address") String walletAddress);

    @GET("/v1/tokens/{wallet_address}/{contract_address}")
    Single<ERC20Token> getToken(
            @Path("wallet_address") String walletAddress,
            @Path("contract_address") String contractAddress
    );

    @GET("/v1/collectibles/{wallet_address}")
    Single<ERC721Tokens> getCollectibles(@Path("wallet_address") String walletAddress);

    @GET("/v1/collectibles/{wallet_address}/{contract_address}")
    Single<ERC721TokenWrapper> getCollectible(
            @Path("wallet_address") String walletAddress,
            @Path("contract_address") String contactAddress
    );

    @POST("/v1/token")
    Completable addCustomToken(
            @Query("timestamp") long timestamp,
            @Body CustomERCToken customERCToken
    );
}
