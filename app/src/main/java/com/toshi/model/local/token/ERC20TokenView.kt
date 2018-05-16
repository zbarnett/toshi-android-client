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

package com.toshi.model.local.token

import android.content.Intent
import com.toshi.model.network.token.ERC20Token
import com.toshi.view.activity.ViewTokenActivity

data class ERC20TokenView(
        val symbol: String?,
        val name: String?,
        val balance: String?,
        val decimals: Int?,
        val contractAddress: String?,
        val icon: String?
) : Token() {

    companion object {
        private const val SYMBOL = "symbol"
        private const val NAME = "name"
        private const val DECIMALS = "decimals"
        private const val BALANCE = "value"
        private const val CONTRACT_ADDRESS = "contract_address"
        private const val ICON = "icon"

        fun buildIntent(intent: Intent, ERCToken: ERC20TokenView): Intent {
            return intent.apply {
                putExtra(SYMBOL, ERCToken.symbol)
                putExtra(NAME, ERCToken.name)
                putExtra(BALANCE, ERCToken.balance)
                putExtra(DECIMALS, ERCToken.decimals)
                putExtra(CONTRACT_ADDRESS, ERCToken.contractAddress)
                putExtra(ICON, ERCToken.icon)
                putExtra(ViewTokenActivity.TOKEN_TYPE, ViewTokenActivity.ERC20_TOKEN)
            }
        }

        fun getTokenFromIntent(intent: Intent): ERC20TokenView? {
            if (!hasAllExtras(intent)) return null
            return ERC20TokenView(
                    intent.getStringExtra(SYMBOL),
                    intent.getStringExtra(NAME),
                    intent.getStringExtra(BALANCE),
                    intent.getIntExtra(DECIMALS, 0),
                    intent.getStringExtra(CONTRACT_ADDRESS),
                    intent.getStringExtra(ICON)
            )
        }

        private fun hasAllExtras(intent: Intent): Boolean {
            return intent.hasExtra(SYMBOL) && intent.hasExtra(NAME)
                    && intent.hasExtra(DECIMALS) && intent.hasExtra(BALANCE)
                    && intent.hasExtra(CONTRACT_ADDRESS) && intent.hasExtra(ICON)
        }

        fun map(ERCToken: ERC20Token): ERC20TokenView {
            return ERC20TokenView(
                    symbol = ERCToken.symbol,
                    name = ERCToken.name,
                    balance = ERCToken.balance,
                    decimals = ERCToken.decimals,
                    contractAddress = ERCToken.contractAddress,
                    icon = ERCToken.icon
            )
        }
    }
}