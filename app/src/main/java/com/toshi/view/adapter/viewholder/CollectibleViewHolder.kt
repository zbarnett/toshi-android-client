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
import com.toshi.crypto.util.TypeConverter
import com.toshi.model.network.ERC721Token
import com.toshi.util.ImageUtil
import kotlinx.android.synthetic.main.list_item__collectible.view.*

class CollectibleViewHolder(private val collectibleName: String?, itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun setCollectible(collectible: ERC721Token) {
        val name = getName(collectible)
        if (name != null) itemView.name.text = name
        if (collectible.image != null) ImageUtil.loadFromNetwork(collectible.image, itemView.avatar)
        if (collectible.description != null) itemView.description.text = collectible.description
    }

    private fun getName(collectible: ERC721Token): String? {
        val collectibleName = this.collectibleName ?: ""
        val tokenId = TypeConverter.fromHexToDecimal(collectible.tokenId ?: "0x0")
        return collectible.name ?: "$collectibleName #$tokenId"
    }
}