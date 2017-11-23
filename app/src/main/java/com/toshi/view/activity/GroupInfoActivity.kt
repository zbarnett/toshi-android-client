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
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.viewModel.GroupInfoViewModel


class GroupInfoActivity : AppCompatActivity() {
    companion object {
        const val EXTRA__GROUP_ID = "extra_group_id"
    }

    private lateinit var viewModel: GroupInfoViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        init()
    }

    private fun init() {
        initViewModel()
        processIntentData()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GroupInfoViewModel::class.java)
    }

    private fun processIntentData() {
        val groupId = getGroupIdFromIntent()
    }

    private fun getGroupIdFromIntent(): String? = intent.getStringExtra(EXTRA__GROUP_ID)
}