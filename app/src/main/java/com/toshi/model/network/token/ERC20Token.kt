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

package com.toshi.model.network.token

import com.squareup.moshi.Json
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ERC20Token : RealmObject() {

    @PrimaryKey
    var primaryKey: String? = null
    var walletIndex: Int? = null
    var networkId: String? = null
    @Json(name = "contract_address")
    var contractAddress: String? = null
    var symbol: String? = null
    var name: String? = null
    var balance: String? = null
    var decimals: Int? = null
    var icon: String? = null
    var cacheTimestamp: Long? = System.currentTimeMillis()

    companion object {
        private const val CACHE_TIMEOUT = 1000 * 60 * 5
    }

    fun setPrimaryKey(contractAddress: String, walletIndex: Int, networkId: String) {
        primaryKey = "$contractAddress/$walletIndex/$networkId"
    }

    fun needsRefresh(): Boolean = System.currentTimeMillis() - (cacheTimestamp ?: 0) > CACHE_TIMEOUT
}