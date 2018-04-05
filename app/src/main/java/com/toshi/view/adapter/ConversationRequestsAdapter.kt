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
import com.toshi.view.adapter.viewholder.ConversationRequestsViewHolder

class ConversationRequestsAdapter(
        private val onRequestsClickListener: () -> Unit
) : BaseCompoundableAdapter<ConversationRequestsViewHolder, Int>() {

    private var unacceptedConversations: List<Conversation>? = null

    fun setUnacceptedConversations(conversations: List<Conversation>) {
        val priorUnaccepted = unacceptedConversations?.count() ?: 0
        unacceptedConversations = conversations

        if (conversations.count() > 0) {
            setItemList(listOf(conversations.count()))
        } else {
            // Simply setting the empty list can cause a crash
            if (priorUnaccepted > 0) {
                // Something was set before, remove it
                removeItemAtIndex(0)
            } else {
                // Nothing was set before, set up a new set of conversations.
                setItemList(listOf())
            }
        }
    }

    fun removeConversation(conversation: Conversation) {
        unacceptedConversations?.let { unaccepted ->
            if (!unaccepted.contains(conversation)) {
                return
            }

            mutateConversations { it.remove(conversation) }
        }
    }

    fun addOrUpdateConversation(conversation: Conversation) {
        unacceptedConversations?.let { updated ->
            val existing = updated.firstOrNull { current -> current.threadId == conversation.threadId }
            if (existing != null) {
                mutateConversations { mutableConversations ->
                    val index = mutableConversations.indexOf(existing)
                    mutableConversations.removeAt(index)
                    mutableConversations.add(index, conversation)
                }
            } else {
                mutateConversations { it.add(conversation) }
            }
        }
    }

    private fun mutateConversations(action: (MutableList<Conversation>) -> Unit) {
        unacceptedConversations?.let {
            val mutableConversations = it.toMutableList()
            action(mutableConversations)
            setUnacceptedConversations(mutableConversations.toList())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationRequestsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item__conversation_requests, parent, false)
        return ConversationRequestsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConversationRequestsViewHolder, position: Int) {
        unacceptedConversations?.let {
            holder.setNumberOfConversationRequests(it.count())
                    .setOnItemClickListener(onRequestsClickListener)
                    .loadAvatar(it)
        }
    }

    override fun compoundableBindViewHolder(viewHolder: RecyclerView.ViewHolder, adapterIndex: Int) {
        val typedHolder = viewHolder as? ConversationRequestsViewHolder ?: return
        onBindViewHolder(typedHolder, adapterIndex)
    }
}