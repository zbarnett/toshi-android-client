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
import com.toshi.model.local.Conversation
import com.toshi.model.local.User
import com.toshi.util.keyboard.SOFAMessageFormatter
import com.toshi.view.adapter.viewholder.ConversationRequestViewHolder

class ConversationRequestAdapter(
        private val onItemCLickListener: (Conversation) -> Unit,
        private val onAcceptClickListener: (Conversation) -> Unit,
        private val onRejectClickListener: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationRequestViewHolder>() {

    private val conversations = mutableListOf<Conversation>()
    private var messageFormatter: SOFAMessageFormatter? = null

    fun setConversations(localUser: User?, conversations: List<Conversation>) {
        initMessageFormatter(localUser)
        this.conversations.clear()
        this.conversations.addAll(conversations)
        notifyDataSetChanged()
    }

    private fun initMessageFormatter(localUser: User?) {
        if (messageFormatter != null) return
        messageFormatter = SOFAMessageFormatter(localUser)
    }

    fun addConversation(conversation: Conversation) {
        val index = conversations.indexOf(conversation)
        if (index == -1) {
            conversations.add(0, conversation)
            notifyItemInserted(0)
            return
        }

        conversations[index] = conversation
        notifyItemChanged(index)
    }

    fun remove(conversation: Conversation) {
        val index = conversations.indexOf(conversation)
        conversations.remove(conversation)
        notifyItemRemoved(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationRequestViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ConversationRequestViewHolder(layoutInflater.inflate(R.layout.list_item__conversation_request, parent, false))
    }

    override fun onBindViewHolder(holder: ConversationRequestViewHolder, position: Int) {
        val conversation = conversations[position]
        val formattedLatestMessage = formatLatestMessage(conversation)
        holder
                .setConversation(conversation)
                .setLatestMessage(formattedLatestMessage)
                .setOnItemClickListener(conversation, onItemCLickListener)
                .setOnAcceptClickListener(conversation, onAcceptClickListener)
                .setOnRejectClickListener(conversation, onRejectClickListener)
    }

    private fun formatLatestMessage(conversation: Conversation): String {
        return if (conversation.latestMessage != null) {
            messageFormatter?.formatMessage(conversation.latestMessage) ?: ""
        } else ""
    }

    override fun getItemCount() = conversations.size
}