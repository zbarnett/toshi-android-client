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
import com.toshi.model.network.user.UserType
import com.toshi.view.activity.ChatSearchActivity.Companion.TYPE
import com.toshi.view.activity.ChatSearchActivity.Companion.VIEW_PROFILE
import com.toshi.view.adapter.CompoundAdapter
import com.toshi.view.adapter.ListSectionAdapter
import com.toshi.view.adapter.PopularBotsAdapter
import com.toshi.view.adapter.PopularUsersAdapter
import com.toshi.viewModel.PopularSearchViewModel
import kotlinx.android.synthetic.main.activity_popular_user_search.closeButton
import kotlinx.android.synthetic.main.activity_popular_user_search.search
import kotlinx.android.synthetic.main.activity_popular_user_search.searchList
import kotlinx.android.synthetic.main.activity_view_popular_users.loadingSpinner

class PopularUserSearchActivity : AppCompatActivity() {

    private lateinit var viewModel: PopularSearchViewModel
    private lateinit var compoundAdapter: CompoundAdapter
    private lateinit var botsAdapter: PopularBotsAdapter
    private lateinit var groupsAdapter: PopularUsersAdapter
    private lateinit var usersAdapter: PopularUsersAdapter

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
        val botSection = ListSectionAdapter(
                title = getString(R.string.popular_bots),
                clickableString = getString(R.string.see_more),
                onSectionClickedListener = { startViewPopularUsersActivity(UserType.BOT) }
        )

        botsAdapter = PopularBotsAdapter { startProfileActivity(it) }

        val groupSection = ListSectionAdapter(
                title = getString(R.string.popular_groups),
                clickableString = getString(R.string.see_more),
                onSectionClickedListener = { startViewPopularUsersActivity(UserType.GROUPBOT) }
        )

        groupsAdapter = PopularUsersAdapter { startProfileActivity(it) }

        val userSection = ListSectionAdapter(
                title = getString(R.string.popular_users),
                clickableString = getString(R.string.see_more),
                onSectionClickedListener = { startViewPopularUsersActivity(UserType.USER) }
        )

        usersAdapter = PopularUsersAdapter { startProfileActivity(it) }

        compoundAdapter = CompoundAdapter(
                listOf(
                        botSection,
                        botsAdapter,
                        groupSection,
                        groupsAdapter,
                        userSection,
                        usersAdapter
                )
        )

        searchList.apply {
            adapter = compoundAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun startViewPopularUsersActivity(userType: UserType) {
        startActivity<ViewPopularUsersActivity> { putExtra(ViewPopularUsersActivity.TYPE, userType) }
    }

    private fun startProfileActivity(user: User) {
        startActivity<ViewUserActivity> { putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, user.toshiId) }
    }

    private fun initObservers() {
        viewModel.popularBots.observe(this, Observer {
            if (it != null) botsAdapter.setItemList(it)
        })
        viewModel.popularGroups.observe(this, Observer {
            if (it != null) groupsAdapter.setItemList(it)
        })
        viewModel.popularUsers.observe(this, Observer {
            if (it != null) usersAdapter.setItemList(it)
        })
        viewModel.error.observe(this, Observer {
            if (it != null) toast(it)
        })
        viewModel.isLoading.observe(this, Observer {
            if (it != null) loadingSpinner.isVisible(it)
        })
    }
}