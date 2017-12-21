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
import com.toshi.model.local.Conversation
import com.toshi.view.adapter.listeners.OnUpdateListener
import kotlinx.android.synthetic.main.list_item__conversation_requests.view.*

class ConversationRequestsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun setNumberOfConversationRequests(numberOfRequests: Int): ConversationRequestsViewHolder {
        val numberOfRequestsText = itemView.context.getString(R.string.number_of_message_requests, numberOfRequests)
        itemView.messageRequests.text = numberOfRequestsText
        return this
    }

    fun loadAvatar(conversations: List<Conversation>) {
        val lastTwoElements = conversations.takeLast(2)
                .map { it.recipient }
        itemView.avatar.loadAvatars(lastTwoElements)
    }

    fun setOnItemClickListener(onItemClickListener: OnUpdateListener): ConversationRequestsViewHolder {
        itemView.setOnClickListener { onItemClickListener.onUpdate() }
        return this
    }
}