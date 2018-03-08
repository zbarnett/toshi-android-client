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
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toshi.R
import com.toshi.extensions.isVisible
import com.toshi.extensions.isWebUrl
import com.toshi.extensions.openWebView
import com.toshi.extensions.startActivity
import com.toshi.extensions.toast
import com.toshi.model.local.dapp.DappCategory
import com.toshi.model.network.dapp.Dapp
import com.toshi.model.network.dapp.DappSections
import com.toshi.util.KeyboardUtil
import com.toshi.view.activity.ViewAllDappsActivity
import com.toshi.view.activity.ViewAllDappsActivity.Companion.ALL
import com.toshi.view.activity.ViewAllDappsActivity.Companion.CATEGORY
import com.toshi.view.activity.ViewAllDappsActivity.Companion.CATEGORY_ID
import com.toshi.view.activity.ViewAllDappsActivity.Companion.CATEGORY_NAME
import com.toshi.view.activity.ViewAllDappsActivity.Companion.VIEW_TYPE
import com.toshi.view.activity.ViewDappActivity
import com.toshi.view.activity.ViewDappActivity.Companion.DAPP_ID
import com.toshi.view.adapter.DappAdapter
import com.toshi.view.adapter.SearchDappAdapter
import com.toshi.viewModel.DappViewModel
import kotlinx.android.synthetic.main.fragment_dapps.dapps
import kotlinx.android.synthetic.main.fragment_dapps.header
import kotlinx.android.synthetic.main.fragment_dapps.searchDapps
import kotlinx.android.synthetic.main.view_collapsing_toshi.input
import kotlinx.android.synthetic.main.view_collapsing_toshi.view.input

class DappFragment : Fragment(), TopLevelFragment {

    companion object {
        private const val TAG = "DappFragment"
    }

    private lateinit var viewModel: DappViewModel
    private lateinit var dappAdapter: DappAdapter
    private lateinit var searchDappAdapter: SearchDappAdapter

    override fun getFragmentTag() = TAG

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_dapps, container, false)
    }

    override fun onViewCreated(view: View?, inState: Bundle?) = init()

    private fun init() {
        initViewModel()
        initAdapters()
        setRecyclerViewVisibility()
        initListeners()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this.activity).get(DappViewModel::class.java)
    }

    private fun initAdapters() {
        initBrowseAdapter()
        initSearchAdapter()
    }

    private fun initBrowseAdapter() {
        dappAdapter = DappAdapter().apply {
            onDappClickedListener = { startActivity<ViewDappActivity> {
                putExtra(DAPP_ID, it.dappId)
            } }
            onFooterClickedListener = { startActivity<ViewAllDappsActivity> {
                putExtra(CATEGORY_NAME, getString(R.string.all_dapps))
                putExtra(VIEW_TYPE, ALL)
            } }
            onCategoryClickedListener = { startActivity<ViewAllDappsActivity> {
                putExtra(CATEGORY_NAME, it.category)
                putExtra(CATEGORY_ID, it.categoryId)
                putExtra(VIEW_TYPE, CATEGORY)
            } }
        }
        dapps.apply {
            adapter = dappAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initSearchAdapter() {
        searchDappAdapter = SearchDappAdapter().apply {
            onSearchClickListener = { openBrowserAndSearchGoogle(it) }
            onGoToClickListener = { openWebView(it) }
            onItemClickedListener = { startActivity<ViewDappActivity> {
                putExtra(DAPP_ID, it.dappId)
            } }
        }
        searchDapps.apply {
            adapter = searchDappAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
        }
    }

    private fun openBrowserAndSearchGoogle(searchValue: String) {
        val address = "https://www.google.com/search?q=$searchValue"
        openWebView(address)
    }

    private fun setRecyclerViewVisibility() {
        if (header.input.text.isEmpty()) showBrowseUI()
        else showSearchUI(header.input.text.toString())
    }

    private fun initListeners() {
        header.onTextChangedListener = { showSearchUI(it) }
        header.onHeaderCollapsed = { showSearchUI(input.text.toString()) }
        header.onHeaderExpanded = { showBrowseUI(); hideKeyboardAndUnfocus() }
    }

    private fun showBrowseUI() {
        dapps.isVisible(true)
        searchDapps.isVisible(false)
    }

    private fun hideKeyboardAndUnfocus() {
        KeyboardUtil.hideKeyboard(header.input)
        header.input.clearFocus()
    }

    private fun showSearchUI(input: String) {
        searchDapps.isVisible(true)
        dapps.isVisible(false)
        if (input.isEmpty()) setSearchEmptyState()
        else search(input)
    }

    private fun setSearchEmptyState() {
        viewModel.getAllDapps()
        val dapps = viewModel.allDapps.value ?: emptyList()
        val category = DappCategory(getString(R.string.dapps), -1)
        searchDappAdapter.setEmptyState(dapps, category)
    }

    private fun search(input: String) {
        viewModel.search(input)
        searchDappAdapter.addGoogleSearchItems(input)
        if (isWebUrl(input)) searchDappAdapter.addWebUrlItems(input)
        else searchDappAdapter.removeWebUrl()
    }

    private fun initObservers() {
        viewModel.dappSections.observe(this, Observer {
            if (it != null) setDapps(it)
        })
        viewModel.dappsError.observe(this, Observer {
            if (it != null) toast(it)
        })
        viewModel.searchResult.observe(this, Observer {
            if (it != null && input.text.isNotEmpty()) setSearchResult(it)
        })
        viewModel.allDapps.observe(this, Observer {
            if (it != null) setSearchResult(it)
        })
    }

    private fun setDapps(dappSections: DappSections) {
        if (dappSections.categories.isEmpty() || dappSections.sections.isEmpty()) return
        dappAdapter.setDapps(dappSections)
    }

    private fun setSearchResult(dapps: List<Dapp>) {
        val dappsCategory = DappCategory(getString(R.string.dapps), -1)
        searchDappAdapter.setDapps(dapps, dappsCategory)
    }
}