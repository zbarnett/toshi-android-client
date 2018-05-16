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
import com.toshi.model.local.token.ERC721TokenInfoView

data class ERC721TokenInfo(
        val symbol: String?,
        val name: String?,
        val balance: String?,
        @Json(name = "contract_address")
        val contractAddress: String?,
        val icon: String?
) {
    fun mapToViewModel(): ERC721TokenInfoView {
        return ERC721TokenInfoView(
                symbol = this.symbol,
                name = this.name,
                balance = this.balance,
                contractAddress = this.contractAddress,
                icon = this.icon
        )
    }
}