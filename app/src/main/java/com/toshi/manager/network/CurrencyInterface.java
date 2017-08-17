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

import com.toshi.model.network.Currencies;
import com.toshi.model.network.ExchangeRate;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Single;

public interface CurrencyInterface {

    @GET("/v1/rates/ETH/{code}")
    Single<ExchangeRate> getRates(@Path("code") String code);

    @GET("/v1/currencies")
    Single<Currencies> getCurrencies();
}