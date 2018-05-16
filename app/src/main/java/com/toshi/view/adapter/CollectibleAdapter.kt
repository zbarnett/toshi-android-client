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
import com.toshi.model.local.token.ERC721TokenView
import com.toshi.view.adapter.viewholder.CollectibleViewHolder

class CollectibleAdapter(private val collectibleName: String?) : RecyclerView.Adapter<CollectibleViewHolder>() {

    private var collectibles: MutableList<ERC721TokenView> = mutableListOf()

    fun setCollectibles(collectibles: List<ERC721TokenView>) {
        if (collectibles.isEmpty()) return
        this.collectibles.clear()
        this.collectibles.addAll(collectibles)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectibleViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item__collectible, parent, false)
        return CollectibleViewHolder(collectibleName, v)
    }

    override fun onBindViewHolder(holder: CollectibleViewHolder, position: Int) {
        val collectible = collectibles[position]
        holder.setCollectible(collectible)
    }

    override fun getItemCount() = collectibles.size
}