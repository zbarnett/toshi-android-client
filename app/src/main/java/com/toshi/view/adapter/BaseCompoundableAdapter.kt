/*
 * 	Copyright (c) 2018. Toshi Inc
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

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import com.toshi.R
import com.toshi.view.BaseApplication

abstract class BaseCompoundableAdapter<VH : RecyclerView.ViewHolder, T> : RecyclerView.Adapter<VH>(), CompoundableAdapter {
    var parent: CompoundAdapter? = null

    private var items: List<T> = listOf()
    private var itemsToRemove: MutableList<T> = mutableListOf()

    open fun deleteItem(item: T) {
        // No-op by default - override if you need to actually delete something persisted.
    }

    override fun getItemCount(): Int = items.size

    override fun getCompoundableItemCount(): Int = itemCount

    override fun compoundableCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return onCreateViewHolder(parent, 0)
    }

    override fun removeItemAtWithUndo(adapterIndex: Int, parentView: RecyclerView) {
        val item = items[adapterIndex]
        removeItemWithUndo(item, parentView)
    }

    override fun doDelete() {
        for (item in itemsToRemove) {
            deleteItem(item)
        }
    }

    override fun setCompoundParent(parent: CompoundAdapter?) {
        this.parent = parent
    }

    fun itemAt(index: Int): T = items[index]

    fun safelyAt(index: Int): T? {
        if (!items.indices.contains(index)) {
            return null
        }

        return itemAt(index)
    }

    open fun setItemList(items: List<T>) {
        this.items = items
        parent?.notifyDataSetChanged(this)
        notifyDataSetChanged()
    }

    private fun mutateItems(action: (MutableList<T>) -> Unit) {
        val mutableCopy = items.toMutableList()
        action(mutableCopy)
        items = mutableCopy.toList()
    }

    fun addItem(item: T) {
        mutateItems { it.add(item) }
        parent?.notifyDataSetChanged(this)
        notifyDataSetChanged()
    }

    fun insertItem(item: T, index: Int) {
        mutateItems { it.add(index, item) }
        parent?.notifyItemInserted(this, index)
        notifyItemInserted(index)
    }

    fun removeItem(item: T) {
        if (!items.contains(item)) {
            return
        }

        val removalIndex = items.indexOf(item)
        mutateItems { it.remove(item) }
        parent?.notifyItemRemoved(this, removalIndex)
        notifyItemRemoved(removalIndex)
    }

    fun removeItemAtIndex(index: Int) {
        if (!items.indices.contains(index)) {
            return
        }

        val itemToRemove = items[index]
        removeItem(itemToRemove)
    }

    fun removeItemWithUndo(removedItem: T, parentView: RecyclerView) {
        val removedIndex = this.items.indexOf(removedItem)
        if (removedIndex < 0) {
            // This is not in the list.
            return
        }

        val snackbar = generateSnackbar(parentView)
        snackbar.setAction(
                R.string.undo,
                { handleUndo(removedIndex, removedItem, parentView) }
        ).show()
        removeItem(removedItem)
        itemsToRemove.add(removedItem)
    }

    private fun handleUndo(adapterPosition: Int, removedItem: T, parentView: RecyclerView) {
        // Put the item back into the list
        insertItem(removedItem, adapterPosition)
        itemsToRemove.remove(removedItem)

        // Scroll to it with or without a parent.
        if (parent != null) {
            parent?.scrollToPosition(this, adapterPosition, parentView)
        } else {
            parentView.scrollToPosition(adapterPosition)
        }
    }

    private fun generateSnackbar(parentView: RecyclerView): Snackbar {
        val snackbar = Snackbar
                .make(parentView, R.string.conversation_deleted, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(BaseApplication.get(), R.color.colorAccent))

        val snackbarView = snackbar.view
        val snackbarTextView = snackbarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        snackbarTextView.setTextColor(ContextCompat.getColor(BaseApplication.get(), R.color.textColorContrast))
        return snackbar
    }
}
