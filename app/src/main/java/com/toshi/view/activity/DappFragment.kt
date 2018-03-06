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
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toshi.R
import com.toshi.extensions.toast
import com.toshi.model.network.TempDapp
import com.toshi.view.adapter.DappAdapter
import com.toshi.view.fragment.toplevel.TopLevelFragment
import com.toshi.viewModel.DappViewModel
import kotlinx.android.synthetic.main.fragment_dapps.*

class DappFragment : Fragment(), TopLevelFragment {

    companion object {
        private const val TAG = "DappFragment"
    }

    private lateinit var dappAdapter: DappAdapter
    private lateinit var viewModel: DappViewModel

    override fun getFragmentTag() = TAG

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_dapps, container, false)
    }

    override fun onViewCreated(view: View?, inState: Bundle?) = init()

    private fun init() {
        initViewModel()
        initAdapter()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this.activity).get(DappViewModel::class.java)
    }

    private fun initAdapter() {
        dappAdapter = DappAdapter()
        dapps.apply {
            adapter = dappAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initObservers() {
        viewModel.dapps.observe(this, Observer {
            if (it != null) setDapps(it)
        })
        viewModel.dappsError.observe(this, Observer {
            if (it != null) toast(it)
        })
    }

    private fun setDapps(dapps: List<TempDapp>) = dappAdapter.setDapps(dapps)
}