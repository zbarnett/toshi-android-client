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

package com.toshi.view.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.toshi.R
import com.toshi.crypto.util.TypeConverter
import com.toshi.extensions.getColorById
import com.toshi.extensions.isVisible
import com.toshi.model.local.token.ERC20TokenView
import com.toshi.model.local.token.ERC721TokenInfoView
import com.toshi.model.local.token.EtherToken
import com.toshi.model.local.token.Token
import com.toshi.util.EthUtil
import com.toshi.util.ImageUtil
import kotlinx.android.synthetic.main.list_item__token.view.avatar
import kotlinx.android.synthetic.main.list_item__token.view.erc20Abbreviation
import kotlinx.android.synthetic.main.list_item__token.view.erc20Name
import kotlinx.android.synthetic.main.list_item__token.view.erc20Wrapper
import kotlinx.android.synthetic.main.list_item__token.view.erc721Name
import kotlinx.android.synthetic.main.list_item__token.view.erc721Wrapper
import kotlinx.android.synthetic.main.list_item__token.view.fiatValue
import kotlinx.android.synthetic.main.list_item__token.view.value

class TokensViewHolder(private val tokenType: TokenType, itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun setToken(token: Token, ERC20Listener: ((Token) -> Unit)?, ERC721Listener: ((ERC721TokenInfoView) -> Unit)?) {
        when (tokenType) {
            is TokenType.ERC20Token -> showToken(token, ERC20Listener)
            is TokenType.ERC721Token -> showERC721InfoView(token, ERC721Listener)
        }
    }

    private fun showToken(token: Token, tokenListener: ((Token) -> Unit)?) {
        when (token) {
            is EtherToken -> showEtherToken(token, tokenListener)
            is ERC20TokenView -> showERC20View(token, tokenListener)
            else -> throw IllegalStateException(Throwable("Invalid token type in this context"))
        }
    }

    private fun showERC721InfoView(token: Token, tokenListener: ((ERC721TokenInfoView) -> Unit)?) {
        when (token) {
            is ERC721TokenInfoView -> showERC721InfoView(token, tokenListener)
            else -> throw IllegalStateException(Throwable("Invalid token type in this context"))
        }
    }

    private fun showEtherToken(etherToken: EtherToken, tokenListener: ((Token) -> Unit)?) {
        itemView.erc20Wrapper.visibility = View.VISIBLE
        itemView.erc721Wrapper.visibility = View.GONE
        itemView.erc20Name.text = etherToken.name
        itemView.erc20Abbreviation.text = etherToken.symbol
        itemView.value.text = etherToken.etherValue
        itemView.fiatValue.text = etherToken.fiatValue
        itemView.fiatValue.isVisible(true)
        itemView.value.setTextColor(itemView.getColorById(R.color.textColorPrimary))
        itemView.setOnClickListener { tokenListener?.invoke(etherToken) }
        itemView.avatar.setImageResource(etherToken.icon)
    }

    private fun showERC20View(ERCToken: ERC20TokenView, tokenListener: ((ERC20TokenView) -> Unit)?) {
        itemView.erc20Wrapper.visibility = View.VISIBLE
        itemView.erc721Wrapper.visibility = View.GONE
        itemView.erc20Name.text = ERCToken.name
        itemView.erc20Abbreviation.text = ERCToken.symbol
        itemView.value.text = TypeConverter.formatHexString(ERCToken.balance, ERCToken.decimals ?: 0, EthUtil.ETH_FORMAT)
        itemView.fiatValue.isVisible(false)
        itemView.value.setTextColor(itemView.getColorById(R.color.textColorPrimary))
        loadImage(ERCToken.icon, itemView.avatar)
        itemView.setOnClickListener { tokenListener?.invoke(ERCToken) }
    }

    private fun showERC721InfoView(ERCToken: ERC721TokenInfoView, tokenListener: ((ERC721TokenInfoView) -> Unit)?) {
        itemView.erc721Wrapper.visibility = View.VISIBLE
        itemView.erc20Wrapper.visibility = View.GONE
        itemView.erc721Name.text = ERCToken.name
        itemView.value.text = TypeConverter.fromHexToDecimal(ERCToken.balance ?: "0x0")
        itemView.value.setTextColor(itemView.getColorById(R.color.textColorSecondary))
        loadImage(ERCToken.icon, itemView.avatar)
        itemView.setOnClickListener { tokenListener?.invoke(ERCToken) }
    }

    private fun loadImage(url: String?, imageView: ImageView) {
        if (url == null) imageView.setImageResource(R.color.placeholder)
        else ImageUtil.load(url, imageView)
    }
}

sealed class TokenType {
    class ERC20Token : TokenType()
    class ERC721Token : TokenType()
}