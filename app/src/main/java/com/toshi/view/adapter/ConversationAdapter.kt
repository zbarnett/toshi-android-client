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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.toshi.R
import com.toshi.model.local.Conversation
import com.toshi.model.local.User
import com.toshi.util.keyboard.SOFAMessageFormatter
import com.toshi.view.adapter.viewholder.ThreadViewHolder

class ConversationAdapter(
        private val onItemClickListener: (Conversation) -> Unit,
        private val onItemLongClickListener: (Conversation) -> Unit,
        private val onItemDeleted: (Conversation) -> Unit
) : BaseCompoundableAdapter<ThreadViewHolder, Conversation>() {

    private var messageFormatter: SOFAMessageFormatter? = null

    fun setItemList(localUser: User?, items: List<Conversation>) {
        initMessageFormatter(localUser)
        val filteredItems = items.filter { !it.isRecipientInvalid } // Don't show conversations with invalid recipients.
        setItemList(filteredItems)
    }

    private fun initMessageFormatter(localUser: User?) {
        if (messageFormatter != null) return
        messageFormatter = SOFAMessageFormatter(localUser)
    }

    override fun compoundableBindViewHolder(viewHolder: RecyclerView.ViewHolder, adapterIndex: Int) {
        val typedHolder = viewHolder as? ThreadViewHolder
                ?: throw AssertionError("This is not the right type!")
        onBindViewHolder(typedHolder, adapterIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item__recent, parent, false)
        return ThreadViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        val conversation = safelyAt(position)
                ?: throw AssertionError("No conversation at $position")
        holder.setThread(conversation)
        val formattedLatestMessage = formatLatestMessage(conversation)
        holder.setLatestMessage(formattedLatestMessage)
        holder.setOnItemClickListener(conversation, onItemClickListener)
        holder.setOnItemLongClickListener(conversation, onItemLongClickListener)
    }

    private fun formatLatestMessage(conversation: Conversation): String {
        return if (conversation.latestMessage != null) {
            messageFormatter?.formatMessage(conversation.latestMessage) ?: ""
        } else ""
    }

    override fun deleteItem(item: Conversation) = onItemDeleted(item)
}