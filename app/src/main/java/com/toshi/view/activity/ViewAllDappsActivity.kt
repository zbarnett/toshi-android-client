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
import com.toshi.extensions.getPxSize
import com.toshi.extensions.toast
import com.toshi.view.adapter.AllDappsAdapter
import com.toshi.viewModel.ViewAllDappsViewModel
import kotlinx.android.synthetic.main.activity_view_dapps.*

class ViewAllDappsActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewAllDappsViewModel
    private lateinit var allDappsAdapter: AllDappsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_dapps)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        initAdapter()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ViewAllDappsViewModel::class.java)
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
    }

    private fun initAdapter() {
        allDappsAdapter = AllDappsAdapter()
        dapps.apply {
            adapter = allDappsAdapter
            layoutManager = LinearLayoutManager(context)
            addHorizontalLineDivider(leftPadding = getPxSize(R.dimen.avatar_size_medium)
                    + getPxSize(R.dimen.activity_horizontal_margin)
                    + getPxSize(R.dimen.activity_horizontal_margin))
        }
    }

    private fun initObservers() {
        viewModel.dapps.observe(this, Observer {
            if (it != null) allDappsAdapter.setDapps(it)
        })
        viewModel.dappsError.observe(this, Observer {
            if (it != null) toast(it)
        })
    }
}