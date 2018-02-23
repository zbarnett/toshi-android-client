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

package com.toshi.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.model.network.token.ERCToken
import com.toshi.model.network.token.Token
import com.toshi.view.adapter.viewholder.TokenFooterViewHolder
import com.toshi.view.adapter.viewholder.TokenType
import com.toshi.view.adapter.viewholder.TokensViewHolder

class TokenAdapter(private val tokenType: TokenType) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ITEM = 1
        private const val FOOTER = 2
    }

    private val tokens = mutableListOf<Token>()
    var tokenListener: ((Token) -> Unit)? = null
    var ERC721Listener: ((ERCToken) -> Unit)? = null

    fun addTokens(ERCTokens: List<Token>) {
        if (ERCTokens.isEmpty()) return
        this.tokens.clear()
        this.tokens.addAll(ERCTokens)
        this.tokens.add(Footer())
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val item = tokens[position]
        return when (item) {
            is Footer -> FOOTER
            else -> ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        return when (viewType) {
            FOOTER -> {
                val view = layoutInflater.inflate(R.layout.list_item__token_footer, parent, false)
                TokenFooterViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.list_item__token, parent, false)
                TokensViewHolder(tokenType, view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val token = tokens[position]
        when (holder) {
            is TokenFooterViewHolder -> {}
            is TokensViewHolder -> holder.setToken(token, tokenListener, ERC721Listener)
        }
    }

    override fun getItemCount() = tokens.size
}

private class Footer : Token() //Workaround for footer in recyclerview