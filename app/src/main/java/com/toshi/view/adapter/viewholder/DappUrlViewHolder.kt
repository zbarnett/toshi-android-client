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
import com.toshi.R
import com.toshi.extensions.getColorById
import com.toshi.extensions.getString
import com.toshi.extensions.isVisible
import com.toshi.model.local.dapp.DappGoogleSearch
import com.toshi.model.local.dapp.DappUrl
import kotlinx.android.synthetic.main.list_item__dapp_url.view.postfix
import kotlinx.android.synthetic.main.list_item__dapp_url.view.query

class DappUrlViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun setGoogleSearchItem(searchItem: DappGoogleSearch): DappUrlViewHolder {
        itemView.query.setTextColor(itemView.getColorById(R.color.textColorPrimary))
        itemView.query.text = itemView.getString(R.string.value_with_space_at_end, searchItem.searchValue)
        itemView.postfix.isVisible(true)
        return this
    }

    fun setOnGoogleSearchClickListener(searchItem: DappGoogleSearch, listener: (String) -> Unit): DappUrlViewHolder {
        itemView.setOnClickListener { listener(searchItem.searchValue) }
        return this
    }

    fun setDappUrlItem(dappUrl: DappUrl): DappUrlViewHolder {
        itemView.query.setTextColor(itemView.getColorById(R.color.colorPrimary))
        itemView.query.text = itemView.getString(R.string.value_with_space_at_end, dappUrl.url)
        itemView.postfix.isVisible(false)
        return this
    }

    fun setOnGoToClickListener(dappUrl: DappUrl, listener: (String) -> Unit): DappUrlViewHolder {
        itemView.setOnClickListener { listener(dappUrl.url) }
        return this
    }
}