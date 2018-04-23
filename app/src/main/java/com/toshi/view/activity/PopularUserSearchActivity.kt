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
import com.toshi.model.local.User
import com.toshi.model.network.UserSection
import com.toshi.model.network.user.UserType
import com.toshi.view.activity.ChatSearchActivity.Companion.TYPE
import com.toshi.view.activity.ChatSearchActivity.Companion.VIEW_PROFILE
import com.toshi.view.activity.ViewPopularUsersActivity.Companion.SEARCH_QUERY
import com.toshi.view.activity.ViewPopularUsersActivity.Companion.TITLE
import com.toshi.view.adapter.CompoundAdapter
import com.toshi.view.adapter.ListSectionAdapter
import com.toshi.view.adapter.PopularUsersAdapter
import com.toshi.viewModel.PopularSearchViewModel
import kotlinx.android.synthetic.main.activity_popular_user_search.closeButton
import kotlinx.android.synthetic.main.activity_popular_user_search.search
import kotlinx.android.synthetic.main.activity_popular_user_search.searchList
import kotlinx.android.synthetic.main.activity_view_popular_users.loadingSpinner

class PopularUserSearchActivity : AppCompatActivity() {

    private lateinit var viewModel: PopularSearchViewModel

    private lateinit var compoundAdapter: CompoundAdapter

    private lateinit var groupsAdapter: PopularUsersAdapter
    private lateinit var botsAdapter: PopularUsersAdapter
    private lateinit var usersAdapter: PopularUsersAdapter

    private lateinit var groupSectionAdapter: ListSectionAdapter
    private lateinit var botSectionAdapter: ListSectionAdapter
    private lateinit var userSectionAdapter: ListSectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popular_user_search)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        initAdapter()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = getViewModel()
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
        search.setOnClickListener { startActivity<ChatSearchActivity> { putExtra(TYPE, VIEW_PROFILE) } }
    }

    private fun initAdapter() {
        groupSectionAdapter = ListSectionAdapter(
                clickableString = getString(R.string.see_more),
                onSectionClickedListener = { startViewPopularUsersActivity(UserType.GROUPBOT) }
        )

        groupsAdapter = PopularUsersAdapter { startProfileActivity(it) }

        botSectionAdapter = ListSectionAdapter(
                clickableString = getString(R.string.see_more),
                onSectionClickedListener = { startViewPopularUsersActivity(UserType.BOT) }
        )

        botsAdapter = PopularUsersAdapter { startProfileActivity(it) }

        userSectionAdapter = ListSectionAdapter(
                clickableString = getString(R.string.see_more),
                onSectionClickedListener = { startViewPopularUsersActivity(UserType.USER) }
        )

        usersAdapter = PopularUsersAdapter { startProfileActivity(it) }

        compoundAdapter = CompoundAdapter(
                listOf(
                        groupSectionAdapter,
                        groupsAdapter,
                        botSectionAdapter,
                        botsAdapter,
                        userSectionAdapter,
                        usersAdapter
                )
        )

        searchList.apply {
            adapter = compoundAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun startViewPopularUsersActivity(userType: UserType) {
        val searchQuery = getSearchQuery(userType)
        val title = getTitle(userType)
        startActivity<ViewPopularUsersActivity> {
            putExtra(SEARCH_QUERY, searchQuery)
            putExtra(TYPE, userType)
            putExtra(TITLE, title)
        }
    }

    private fun getSearchQuery(userType: UserType): String? {
        return when (userType) {
            UserType.GROUPBOT -> viewModel.groups.value?.query
            UserType.BOT -> viewModel.bots.value?.query
            UserType.USER -> viewModel.users.value?.query
        }
    }

    private fun getTitle(userType: UserType): String? {
        return when (userType) {
            UserType.GROUPBOT -> viewModel.groups.value?.name
            UserType.BOT -> viewModel.bots.value?.name
            UserType.USER -> viewModel.users.value?.name
        }
    }

    private fun startProfileActivity(user: User) {
        startActivity<ViewUserActivity> { putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, user.toshiId) }
    }

    private fun initObservers() {
        viewModel.groups.observe(this, Observer {
            if (it != null) addSection(groupSectionAdapter, groupsAdapter, it)
        })

        viewModel.bots.observe(this, Observer {
            if (it != null) addSection(botSectionAdapter, botsAdapter, it)
        })

        viewModel.users.observe(this, Observer {
            if (it != null) addSection(userSectionAdapter, usersAdapter, it)
        })

        viewModel.error.observe(this, Observer {
            if (it != null) toast(it)
        })
        viewModel.isLoading.observe(this, Observer {
            if (it != null) loadingSpinner.isVisible(it)
        })
    }

    private fun addSection(sectionAdapter: ListSectionAdapter, adapter: PopularUsersAdapter, userSection: UserSection) {
        sectionAdapter.setItemList(listOf(userSection.name.orEmpty()))
        adapter.setItemList(userSection.results)
    }
}