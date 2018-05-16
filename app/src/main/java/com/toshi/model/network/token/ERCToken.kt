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

import com.toshi.model.local.token.ERC20TokenView

data class ERCToken(
        val symbol: String?,
        val name: String?,
        val balance: String?,
        val decimals: Int?,
        val contractAddress: String?,
        val icon: String?
) {
    fun mapToViewModel(): ERC20TokenView {
        return ERC20TokenView(
                symbol = this.symbol,
                name = this.name,
                balance = this.balance,
                decimals = this.decimals,
                contractAddress = this.contractAddress,
                icon = this.icon
        )
    }
}