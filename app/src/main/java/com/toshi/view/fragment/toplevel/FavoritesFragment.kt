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

package com.toshi.view.fragment.toplevel

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.toshi.R
import com.toshi.extensions.addHorizontalLineDivider
import com.toshi.extensions.isVisible
import com.toshi.extensions.startActivity
import com.toshi.extensions.startExternalActivity
import com.toshi.model.local.User
import com.toshi.util.OnSingleClickListener
import com.toshi.view.activity.ContactSearchActivity
import com.toshi.view.activity.ScannerActivity
import com.toshi.view.activity.ViewUserActivity
import com.toshi.view.adapter.UserAdapter
import com.toshi.view.adapter.listeners.OnItemClickListener
import com.toshi.viewModel.FavoritesViewModel
import kotlinx.android.synthetic.main.fragment_favorites.*

class FavoritesFragment : Fragment(), TopLevelFragment {

    companion object {
        private const val TAG = "FavoritesFragment"
    }

    override fun getFragmentTag() = TAG

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var favoriteAdapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, inState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) = init()

    private fun init() {
        initViewModel()
        initMenu()
        initClickListeners()
        initRecyclerView()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity).get(FavoritesViewModel::class.java)
    }

    private fun initMenu() {
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
    }

    private fun initClickListeners() {
        userSearch.setOnClickListener { handleUserSearchClicked.onClick(it) }
        inviteFriends.setOnClickListener { handleInviteFriendsClicked.onClick(it) }
    }

    private val handleUserSearchClicked = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            startActivity<ContactSearchActivity>()
        }
    }

    private val handleInviteFriendsClicked = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            handleInviteFriends()
        }
    }

    private fun initRecyclerView() {
        favoriteAdapter = UserAdapter()
                .apply { onItemClickListener = OnItemClickListener { startViewUserActivity(it) } }

        favorites.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoriteAdapter
            addHorizontalLineDivider()
        }
    }

    private fun startViewUserActivity(user: User) = startActivity<ViewUserActivity> {
        putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, user.toshiId)
    }

    private fun initObservers() {
        viewModel.contacts.observe(this, Observer {
            users -> users?.let { handleUsers(it) }
        })
    }

    private fun handleUsers(users: List<User>) {
        favoriteAdapter.setUsers(users)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val isEmpty = favoriteAdapter.itemCount == 0
        emptyState.isVisible(isEmpty)
        favorites.isVisible(!isEmpty)
    }

    override fun onStart() {
        super.onStart()
        getContacts()
    }

    private fun getContacts() = viewModel.loadContacts()

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.contacts, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.scan_qr -> startActivity<ScannerActivity>()
            R.id.invite_friends -> handleInviteFriends()
            R.id.search_people -> startActivity<ContactSearchActivity>()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleInviteFriends() = startExternalActivity {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_friends_intent_message))
    }
}