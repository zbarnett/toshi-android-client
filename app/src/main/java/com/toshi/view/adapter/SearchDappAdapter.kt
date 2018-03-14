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
import com.toshi.extensions.sameContentAs
import com.toshi.model.local.dapp.DappCategory
import com.toshi.model.local.dapp.DappGoogleSearch
import com.toshi.model.local.dapp.DappGoogleSearchCategory
import com.toshi.model.local.dapp.DappListItem
import com.toshi.model.local.dapp.DappUrl
import com.toshi.model.local.dapp.DappUrlSearchCategory
import com.toshi.model.network.dapp.Dapp
import com.toshi.util.LogUtil
import com.toshi.view.adapter.viewholder.DappUrlViewHolder
import com.toshi.view.adapter.viewholder.SearchDappCategoryViewHolder
import com.toshi.view.adapter.viewholder.SearchDappViewHolder

class SearchDappAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ITEM = 1
        private const val CATEGORY = 2
        private const val GOOGLE_SEARCH = 3
        private const val URL = 4

        private const val FIRST_HEADER_CATEGORY_POSITION = 0
        private const val FIRST_HEADER_ITEM_POSITION = 1
        private const val SECOND_HEADER_CATEGORY_POSITION = 2
        private const val SECOND_HEADER_ITEM_POSITION = 3
    }

    private val dapps by lazy { mutableListOf<DappListItem>() }

    var onSearchClickListener: ((String) -> Unit)? = null
    var onGoToClickListener: ((String) -> Unit)? = null
    var onItemClickedListener: ((Dapp) -> Unit)? = null

    fun setEmptyState(dapps: List<Dapp>, dappsCategory: DappCategory) {
        val dappsWithoutHeader = this.dapps.dropWhile { it !is Dapp }
        if (dappsWithoutHeader.sameContentAs(dapps)) return
        this.dapps.clear()
        this.dapps.add(dappsCategory)
        this.dapps.addAll(dapps)
        notifyDataSetChanged()
    }

    fun setDapps(dapps: List<Dapp>, dappsCategory: DappCategory) {
        val dappsWithoutHeader = this.dapps.dropWhile { it !is Dapp }
        if (dappsWithoutHeader.sameContentAs(dapps)) return
        this.dapps.removeAll { removeItem(it) }
        this.dapps.add(dappsCategory)
        this.dapps.addAll(dapps)
        notifyDataSetChanged()
    }

    //Remove everything except DappGoogleSearch, DappGoogleSearchCategory, DappUrl and DappUrlSearchCategory
    private fun removeItem(listItem: DappListItem): Boolean {
        return listItem !is DappGoogleSearch && listItem !is DappGoogleSearchCategory
                && listItem !is DappUrl && listItem !is DappUrlSearchCategory
    }

    fun addGoogleSearchItems(input: String) {
        val availablePositions = getAvailableGoogleSearchPositions()
        val categoryPosition = availablePositions.first
        val itemPosition = availablePositions.second
        if (input.isNotEmpty()) {
            if (isGoogleSearchItemsAdded()) updateGoogleSearchItem(itemPosition, input)
            else addGoogleSearchItems(itemPosition, categoryPosition, input)
        } else if (isGoogleSearchItemsAdded()) removeGoogleSearchItems()
    }

    private fun updateGoogleSearchItem(itemPosition: Int, input: String) {
        dapps[itemPosition] = DappGoogleSearch(input)
        notifyItemChanged(itemPosition)
    }

    private fun addGoogleSearchItems(itemPosition: Int, categoryPosition: Int, input: String) {
        val category = DappGoogleSearchCategory()
        val googleSearchItem = DappGoogleSearch(input)
        dapps.add(categoryPosition, category)
        dapps.add(itemPosition, googleSearchItem)
        notifyItemChanged(itemPosition)
    }

    private fun removeGoogleSearchItems() {
        dapps.removeAll { it is DappGoogleSearchCategory || it is DappGoogleSearch }
        notifyDataSetChanged()
    }

    private fun getAvailableGoogleSearchPositions(): Pair<Int, Int> {
        return if (isUrlItemsAdded()) SECOND_HEADER_CATEGORY_POSITION to SECOND_HEADER_ITEM_POSITION
        else FIRST_HEADER_CATEGORY_POSITION to FIRST_HEADER_ITEM_POSITION
    }

    private fun isGoogleSearchItemsAdded(): Boolean {
        val availablePositions = getAvailableGoogleSearchPositions()
        val categoryPosition = availablePositions.first
        val itemPosition = availablePositions.second
        return dapps.size > itemPosition + 1
                && dapps[categoryPosition] is DappGoogleSearchCategory
                && dapps[itemPosition] is DappGoogleSearch
    }

    fun addWebUrlItems(url: String) {
        val availablePositions = getAvailableUrlPositions()
        val categoryPosition = availablePositions.first
        val itemPosition = availablePositions.second
        if (isUrlItemsAdded()) updateWebUrlItem(itemPosition, url)
        else addWebUrlItems(itemPosition, categoryPosition, url)
    }

    private fun updateWebUrlItem(itemPosition: Int, url: String) {
        dapps[itemPosition] = DappUrl(url)
        notifyItemChanged(itemPosition)
    }

    private fun addWebUrlItems(itemPosition: Int, categoryPosition: Int, url: String) {
        val category = DappUrlSearchCategory()
        val dappUrl = DappUrl(url)
        dapps.add(categoryPosition, category)
        dapps.add(itemPosition, dappUrl)
        notifyDataSetChanged()
    }

    fun removeWebUrl() {
        if (!isUrlItemsAdded()) return
        dapps.removeAll { it is DappUrlSearchCategory || it is DappUrl }
        notifyItemRangeRemoved(FIRST_HEADER_CATEGORY_POSITION, 2)
    }

    private fun isUrlItemsAdded(): Boolean {
        val availablePositions = getAvailableUrlPositions()
        val categoryPosition = availablePositions.first
        val itemPosition = availablePositions.second
        return dapps.size > itemPosition + 1
                && dapps[categoryPosition] is DappUrlSearchCategory
                && dapps[itemPosition] is DappUrl
    }

    private fun getAvailableUrlPositions(): Pair<Int, Int> {
        return FIRST_HEADER_CATEGORY_POSITION to FIRST_HEADER_ITEM_POSITION
    }

    override fun getItemViewType(position: Int): Int {
        val item = dapps[position]
        return when (item) {
            is DappCategory -> CATEGORY
            is DappGoogleSearch -> GOOGLE_SEARCH
            is DappUrl -> URL
            else -> ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        return when (viewType) {
            CATEGORY -> {
                val view = layoutInflater.inflate(R.layout.list_item__search_dapp_category, parent, false)
                SearchDappCategoryViewHolder(view)
            }
            GOOGLE_SEARCH, URL -> {
                val view = layoutInflater.inflate(R.layout.list_item__dapp_url, parent, false)
                DappUrlViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.list_item__dapp_search, parent, false)
                SearchDappViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val dapp = dapps[position]
        when {
            holder is SearchDappViewHolder && dapp is Dapp -> {
                holder.setDapp(dapp)
                        .setOnItemClickedListener(dapp) { onItemClickedListener?.invoke(it) }
            }
            holder is SearchDappCategoryViewHolder && dapp is DappCategory -> holder.setCategory(dapp)
            holder is DappUrlViewHolder && dapp is DappGoogleSearch -> {
                holder.setGoogleSearchItem(dapp)
                        .setOnGoogleSearchClickListener(dapp) { onSearchClickListener?.invoke(it) }
            }
            holder is DappUrlViewHolder && dapp is DappUrl -> {
                holder.setDappUrlItem(dapp)
                        .setOnGoToClickListener(dapp) { onGoToClickListener?.invoke(it) }
            }
            else -> LogUtil.exception(javaClass, "Invalid dapp in this context " + dapp.toString())
        }
    }

    override fun getItemCount() = dapps.size
}