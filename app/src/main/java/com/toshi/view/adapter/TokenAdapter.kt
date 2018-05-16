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

package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.model.local.token.ERC20TokenView
import com.toshi.model.local.token.Token
import com.toshi.view.adapter.viewholder.TokenType
import com.toshi.view.adapter.viewholder.TokensViewHolder

class TokenAdapter(
        private val tokenType: TokenType
) : BaseCompoundableAdapter<TokensViewHolder, Token>() {

    var tokenListener: ((Token) -> Unit)? = null
    var ERC721Listener: ((ERC20TokenView) -> Unit)? = null

    override fun compoundableBindViewHolder(viewHolder: RecyclerView.ViewHolder, adapterIndex: Int) {
        val typedHolder = viewHolder as TokensViewHolder
        onBindViewHolder(typedHolder, adapterIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokensViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item__token, parent, false)
        return TokensViewHolder(tokenType, itemView)
    }

    override fun onBindViewHolder(holder: TokensViewHolder, position: Int) {
        val token = safelyAt(position)
                ?: throw AssertionError("No user at $position")
        holder.setToken(token, tokenListener, ERC721Listener)
    }
}