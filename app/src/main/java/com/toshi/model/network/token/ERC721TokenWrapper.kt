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
import com.toshi.model.local.token.ERC721TokenView
import com.toshi.model.local.token.ERC721TokenWrapperView

data class ERC721TokenWrapper(
        @Json(name = "contract_address")
        val contractAddress: String?,
        val name: String?,
        val icon: String?,
        val url: String?,
        val type: Int?,
        val value: String?,
        val tokens: List<ERC721Token> = emptyList()
) {
    fun mapToViewModel(): ERC721TokenWrapperView {
        return ERC721TokenWrapperView(
                contractAddress = this.contractAddress,
                name = this.name,
                icon = this.name,
                url = this.url,
                type = this.type,
                value = this.value,
                tokens = this.tokens.map { it.mapToViewModel() }
        )
    }
}

data class ERC721Token(
        @Json(name = "contract_address")
        val contractAddress: String?,
        @Json(name = "token_id")
        val tokenId: String?,
        @Json(name = "owner_address")
        val ownerAddress: String?,
        val name: String?,
        val image: String?,
        val description: String?,
        val misc: String?
) {
    fun mapToViewModel(): ERC721TokenView {
        return ERC721TokenView(
                contractAddress = this.contractAddress,
                tokenId = this.tokenId,
                ownerAddress = this.ownerAddress,
                name = this.name,
                image = this.image,
                description = this.description,
                misc = this.misc
        )
    }
}