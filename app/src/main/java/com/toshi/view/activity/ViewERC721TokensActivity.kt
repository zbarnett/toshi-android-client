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
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.toshi.R
import com.toshi.extensions.addHorizontalLineDivider
import com.toshi.extensions.getPxSize
import com.toshi.extensions.getViewModel
import com.toshi.extensions.isVisible
import com.toshi.extensions.toast
import com.toshi.model.local.token.ERC721TokenWrapperView
import com.toshi.view.adapter.CollectibleAdapter
import com.toshi.viewModel.ViewERC721TokensViewModel
import kotlinx.android.synthetic.main.activity_erc721_token_activity.closeButton
import kotlinx.android.synthetic.main.activity_erc721_token_activity.collectibles
import kotlinx.android.synthetic.main.activity_erc721_token_activity.toolbarTitle
import kotlinx.android.synthetic.main.activity_erc721_token_activity.toolbarUrl

class ViewERC721TokensActivity : AppCompatActivity() {

    companion object {
        const val CONTRACT_ADDRESS = "contractAddress"
        const val COLLECTIBLE_NAME = "collectibleName"
    }

    private lateinit var viewModel: ViewERC721TokensViewModel
    private lateinit var collectibleAdapter: CollectibleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_erc721_token_activity)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        initAdapter()
        initObservers()
    }

    private fun initViewModel() {
        val contactAddress = intent.getStringExtra(CONTRACT_ADDRESS)
        if (contactAddress == null) {
            toast(R.string.invalid_token)
            finish()
            return
        }
        viewModel = getViewModel { ViewERC721TokensViewModel(contactAddress) }
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
    }

    private fun initAdapter() {
        val collectibleName = getCollectibleName()
        collectibleAdapter = CollectibleAdapter(collectibleName)
        val lm = LinearLayoutManager(this)
        collectibles.apply {
            layoutManager = lm
            adapter = collectibleAdapter
            addHorizontalLineDivider(leftPadding = getPxSize(R.dimen.avatar_size_medium)
                    + getPxSize(R.dimen.margin_primary)
                    + getPxSize(R.dimen.margin_primary))
        }
    }

    private fun getCollectibleName() = intent.getStringExtra(COLLECTIBLE_NAME)

    private fun initObservers() {
        viewModel.collectible.observe(this, Observer {
            if (it != null) handleCollectible(it)
        })
        viewModel.error.observe(this, Observer {
            if (it != null) toast(it)
        })
    }

    private fun handleCollectible(collectible: ERC721TokenWrapperView) {
        if (collectible.name != null) toolbarTitle.text = collectible.name
        if (collectible.url != null) toolbarUrl.text = collectible.url else toolbarUrl.isVisible(false)
        if (collectible.tokens != null) collectibleAdapter.setCollectibles(collectible.tokens)
    }
}