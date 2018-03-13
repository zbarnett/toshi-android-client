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

package com.toshi.model.network.dapp

import android.content.Intent
import com.squareup.moshi.Json
import com.toshi.model.local.dapp.DappListItem

data class Dapp(
        @Json(name = "dapp_id")
        val dappId: Long?,
        val name: String?,
        val url: String?,
        val description: String?,
        val icon: String?,
        val cover: String?,
        val categories: List<Int> = emptyList()
) : DappListItem() {

    companion object {
        const val DAPP_ID = "dappId"
        const val NAME = "name"
        const val URL = "url"
        const val DESCRIPTION = "description"
        const val ICON = "icon"
        const val COVER = "cover"
        const val CATEGORIES = "categories"

        fun buildIntent(intent: Intent, dapp: Dapp): Intent {
            return intent.apply {
                putExtra(DAPP_ID, dapp.dappId)
                putExtra(NAME, dapp.name)
                putExtra(URL, dapp.url)
                putExtra(DESCRIPTION, dapp.description)
                putExtra(ICON, dapp.icon)
                putExtra(COVER, dapp.cover)
                putIntegerArrayListExtra(CATEGORIES, dapp.categories as ArrayList<Int>)
            }
        }

        fun getDappFromIntent(intent: Intent): Dapp? {
            if (!hasAllExtras(intent)) return null
            return Dapp(
                    intent.getLongExtra(DAPP_ID, -1),
                    intent.getStringExtra(NAME),
                    intent.getStringExtra(URL),
                    intent.getStringExtra(DESCRIPTION),
                    intent.getStringExtra(ICON),
                    intent.getStringExtra(COVER),
                    intent.getIntegerArrayListExtra(CATEGORIES)
            )
        }

        fun hasAllExtras(intent: Intent): Boolean {
            return intent.getLongExtra(DAPP_ID, -1) != -1L &&
                    intent.getStringExtra(NAME) != null &&
                    intent.getStringExtra(URL) != null &&
                    intent.getStringExtra(DESCRIPTION) != null &&
                    intent.getIntegerArrayListExtra(CATEGORIES).size > 0
        }
    }
}
