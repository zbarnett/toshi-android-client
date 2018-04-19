/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 *  This program is free software: you can redistribute it and/or modify
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
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.toshi.R
import com.toshi.extensions.getViewModel
import com.toshi.extensions.isVisible
import com.toshi.extensions.startActivity
import com.toshi.extensions.toast
import com.toshi.model.network.user.UserType
import com.toshi.view.activity.ViewUserActivity.Companion.EXTRA__USER_ADDRESS
import com.toshi.view.adapter.PopularUsersAdapter
import com.toshi.viewModel.ViewPopularUsersViewModel
import kotlinx.android.synthetic.main.activity_view_popular_users.closeButton
import kotlinx.android.synthetic.main.activity_view_popular_users.loadingSpinner
import kotlinx.android.synthetic.main.activity_view_popular_users.toolbarTitle
import kotlinx.android.synthetic.main.activity_view_popular_users.users

class ViewPopularUsersActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewPopularUsersViewModel
    private lateinit var userAdapter: PopularUsersAdapter

    companion object {
        const val TYPE = "type"
        const val SEARCH_QUERY = "searchQuery"
        const val TITLE = "title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_popular_users)
        init()
    }

    private fun init() {
        val type = getViewType()
        val searchQuery = getSearchQuery()
        val title = getToolbarTitle()
        initViewModel(type, searchQuery)
        setToolbarTitle(title)
        initClickListeners()
        initAdapter()
        initObservers()
    }

    private fun getViewType() = intent.getSerializableExtra(TYPE) as? UserType ?: UserType.USER

    private fun getSearchQuery(): String? = intent.getStringExtra(SEARCH_QUERY)

    private fun getToolbarTitle(): String? = intent.getStringExtra(TITLE)

    private fun setToolbarTitle(title: String?) {
        toolbarTitle.text = title.orEmpty()
    }

    private fun initViewModel(userType: UserType, searchQuery: String?) {
        viewModel = getViewModel { ViewPopularUsersViewModel(userType, searchQuery) }
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
    }

    private fun initAdapter() {
        userAdapter = PopularUsersAdapter {
            startActivity<ViewUserActivity> { putExtra(EXTRA__USER_ADDRESS, it.toshiId) }
        }
        users.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initObservers() {
        viewModel.popularUsers.observe(this, Observer {
            if (it != null) userAdapter.setItemList(it)
        })
        viewModel.error.observe(this, Observer {
            if (it != null) toast(it)
        })
        viewModel.isLoading.observe(this, Observer {
            if (it != null) loadingSpinner.isVisible(it)
        })
    }
}