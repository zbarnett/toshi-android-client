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
import android.widget.TextView
import com.toshi.model.local.dapp.DappGoogleSearch
import com.toshi.model.local.dapp.DappUrl

class DappUrlViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun setGoogleSearchItem(searchItem: DappGoogleSearch): DappUrlViewHolder {
        val view = itemView as TextView
        view.text = searchItem.searchValue
        return this
    }

    fun setOnGoogleSearchClickListener(searchItem: DappGoogleSearch, listener: (String) -> Unit): DappUrlViewHolder {
        itemView.setOnClickListener { listener(searchItem.searchValue) }
        return this
    }

    fun setDappUrlItem(dappUrl: DappUrl): DappUrlViewHolder {
        val view = itemView as TextView
        view.text = dappUrl.url
        return this
    }

    fun setOnGoToClickListener(dappUrl: DappUrl, listener: (String) -> Unit): DappUrlViewHolder {
        itemView.setOnClickListener { listener(dappUrl.url) }
        return this
    }
}