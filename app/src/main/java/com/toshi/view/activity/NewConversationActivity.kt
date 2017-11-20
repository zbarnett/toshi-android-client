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

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.extensions.startActivityAndFinish
import com.toshi.model.local.User
import com.toshi.view.fragment.newconversation.GroupParticipantsFragment
import com.toshi.view.fragment.newconversation.UserParticipantsFragment
import com.toshi.viewModel.NewConversationViewModel

class NewConversationActivity : AppCompatActivity() {

    private lateinit var viewModel: NewConversationViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        if (savedInstanceState== null) openFragment(UserParticipantsFragment())
    }

    private fun init() {
        initViewModel()
        initView()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(NewConversationViewModel::class.java)
    }

    private fun initView() = setContentView(R.layout.activity_new_conversation)

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    fun openConversation(user: User) {
        startActivityAndFinish<ChatActivity> {
            putExtra(ChatActivity.EXTRA__THREAD_ID, user.toshiId)
        }
    }

    fun openNewGroupFlow() = openFragment(GroupParticipantsFragment())

    fun openGroupSetupFlow(selectedParticipants: List<User>?) {
        // ToDo
    }
}
