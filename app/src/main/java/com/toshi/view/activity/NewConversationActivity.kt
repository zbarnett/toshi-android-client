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
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.toshi.BuildConfig
import com.toshi.R
import com.toshi.extensions.addHorizontalLineDivider
import com.toshi.extensions.isVisible
import com.toshi.extensions.startActivity
import com.toshi.extensions.startActivityAndFinish
import com.toshi.model.local.User
import com.toshi.util.KeyboardUtil
import com.toshi.view.adapter.UserAdapter
import com.toshi.viewModel.NewConversationViewModel
import kotlinx.android.synthetic.main.activity_new_conversation.*

class NewConversationActivity : AppCompatActivity() {

    private lateinit var viewModel: NewConversationViewModel
    private lateinit var userAdapter: UserAdapter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        initViewModel()
        initView()
        initClickListeners()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(NewConversationViewModel::class.java)
    }

    private fun initView() {
        setContentView(R.layout.activity_new_conversation)
        newGroup.isVisible(BuildConfig.DEBUG)
        initRecyclerView()
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { handleCloseClicked(it) }
        clearButton.setOnClickListener { search.text = null }
        newGroup.setOnClickListener { startActivity<GroupParticipantsActivity>() }
    }

    private fun handleCloseClicked(v: View?) {
        KeyboardUtil.hideKeyboard(v)
        onBackPressed()
    }

    private fun initRecyclerView() {
        userAdapter = UserAdapter().setOnItemClickListener(this::handleUserClicked)
        searchResults.apply {
            layoutManager = LinearLayoutManager(this.context)
            itemAnimator = DefaultItemAnimator()
            adapter = userAdapter
            addHorizontalLineDivider()
        }
    }

    private fun handleUserClicked(user: User) {
        startActivityAndFinish<ChatActivity> {
            putExtra(ChatActivity.EXTRA__THREAD_ID, user.toshiId)
        }
    }

    private fun initObservers() {
        initSearch()
        viewModel.searchResults.observe(this, Observer { searchResults ->
            searchResults?.let { handleSearchResults(it) }
        })
    }

    private fun initSearch() {
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) = updateSearchUi(s.toString().isEmpty())
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = viewModel.queryUpdated(s)
        })
    }

    private fun updateSearchUi(isQueryEmpty: Boolean) {
        clearButton.isVisible(isQueryEmpty)
        if (isQueryEmpty) userAdapter.clear()
    }

    private fun handleSearchResults(searchResults: List<User>) = userAdapter.setUsers(searchResults)
}
