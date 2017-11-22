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

package com.toshi.view.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.toshi.R
import com.toshi.extensions.addHorizontalLineDivider
import com.toshi.model.local.Conversation
import com.toshi.model.local.User
import com.toshi.view.adapter.ConversationRequestAdapter
import com.toshi.viewModel.ConversationRequestViewModel
import kotlinx.android.synthetic.main.activity_conversation_request.*

class ConversationRequestActivity : AppCompatActivity() {

    private lateinit var viewModel: ConversationRequestViewModel
    private lateinit var requestsAdapter: ConversationRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_request)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        initRecyclerView()
        initObservers()
        getConversationsAndLocalUser()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ConversationRequestViewModel::class.java)
    }

    private fun initClickListeners() = closeButton.setOnClickListener { finish() }

    private fun initRecyclerView() {
        requestsAdapter = ConversationRequestAdapter()

        requests.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = requestsAdapter
            addHorizontalLineDivider()
        }
    }

    private fun initObservers() {
        viewModel.conversationsAndLocalUser.observe(this, Observer {
            conversationsAndUser -> conversationsAndUser?.let { handleConversationsAndLocalUser(it.first, it.second) }
        })
        viewModel.updatedConversation.observe(this, Observer {
            updatedConversation -> updatedConversation?.let { requestsAdapter.addConversation(it) }
        })
    }

    private fun handleConversationsAndLocalUser(conversations: List<Conversation>, localUser: User) {
        requestsAdapter.localUser = localUser
        requestsAdapter.setConversations(conversations)
    }

    private fun getConversationsAndLocalUser() = viewModel.getUnacceptedConversationsAndLocalUser()
}