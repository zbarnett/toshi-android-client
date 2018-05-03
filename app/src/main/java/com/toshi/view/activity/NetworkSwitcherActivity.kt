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
import com.toshi.extensions.addHorizontalLineDivider
import com.toshi.extensions.getPxSize
import com.toshi.extensions.getViewModel
import com.toshi.extensions.isVisible
import com.toshi.extensions.toast
import com.toshi.model.local.network.Network
import com.toshi.view.adapter.NetworkAdapter
import com.toshi.viewModel.NetworkSwitcherViewModel
import kotlinx.android.synthetic.main.activity_settings_advanced.closeButton
import kotlinx.android.synthetic.main.activity_settings_advanced.loadingSpinner
import kotlinx.android.synthetic.main.activity_settings_advanced.networkStatusView
import kotlinx.android.synthetic.main.activity_settings_advanced.networks

class NetworkSwitcherActivity : AppCompatActivity() {

    private lateinit var viewModel: NetworkSwitcherViewModel
    private lateinit var networkAdapter: NetworkAdapter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_advanced)
        init()
    }

    private fun init() {
        initViewModel()
        initNetworkView()
        initAdapter()
        initClickListeners()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = getViewModel()
    }

    private fun initNetworkView() {
        networkStatusView.setNetworkVisibility(viewModel.getNetworksInstance())
    }

    private fun initAdapter() {
        networkAdapter = NetworkAdapter { viewModel.changeNetwork(it) }
        networks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = networkAdapter
            itemAnimator = null
            addHorizontalLineDivider(getPxSize(R.dimen.margin_primary))
        }
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
    }

    private fun initObservers() {
        viewModel.networks.observe(this, Observer {
            if (it != null) addNetworks(it.first, it.second)
        })
        viewModel.networkChanged.observe(this, Observer {
            if (it == null) return@Observer
            updateCurrentNetwork(it)
            updateNetworkStatusView()
            toast(R.string.network_changed)
        })
        viewModel.error.observe(this, Observer {
            if (it != null) toast(it)
        })
        viewModel.isLoading.observe(this, Observer {
            if (it != null) loadingSpinner.isVisible(it)
        })
    }

    private fun addNetworks(currentNetwork: Network, networks: List<Network>) {
        networkAdapter.setCurrentNetwork(currentNetwork)
        networkAdapter.setNetworks(networks)
    }

    private fun updateCurrentNetwork(network: Network) = networkAdapter.handleSelectedItem(network)

    private fun updateNetworkStatusView() {
        networkStatusView.setNetworkVisibility(viewModel.getNetworksInstance())
    }
}