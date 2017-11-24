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
import com.toshi.model.local.Conversation
import kotlinx.android.synthetic.main.list_item__conversation_request.view.*

class ConversationRequestViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun setConversation(conversation: Conversation): ConversationRequestViewHolder {
        itemView.name.text = conversation.recipient.displayName
        conversation.recipient.loadAvatarInto(itemView.avatar)
        return this
    }

    fun setLatestMessage(latestMessage: String): ConversationRequestViewHolder {
        itemView.latestMessage.text = latestMessage
        return this
    }

    fun setOnItemClickListener(conversation: Conversation, listener: (Conversation) -> Unit): ConversationRequestViewHolder {
        this.itemView.setOnClickListener { listener(conversation) }
        return this
    }

    fun setOnAcceptClickListener(conversation: Conversation, listener: (Conversation) -> Unit): ConversationRequestViewHolder {
        this.itemView.accept.setOnClickListener { listener(conversation) }
        return this
    }

    fun setOnRejectClickListener(conversation: Conversation, listener: (Conversation) -> Unit): ConversationRequestViewHolder {
        this.itemView.reject.setOnClickListener { listener(conversation) }
        return this
    }
}