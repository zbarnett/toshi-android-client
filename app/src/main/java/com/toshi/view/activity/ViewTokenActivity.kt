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
import com.toshi.R
import com.toshi.crypto.util.TypeConverter
import com.toshi.extensions.getViewModel
import com.toshi.extensions.isVisible
import com.toshi.extensions.startActivity
import com.toshi.extensions.toast
import com.toshi.model.local.token.ERCTokenView
import com.toshi.model.local.token.EtherToken
import com.toshi.model.local.token.Token
import com.toshi.util.EthUtil
import com.toshi.util.ImageUtil
import com.toshi.view.fragment.dialogFragment.ShareWalletAddressDialog
import com.toshi.viewModel.ViewTokenViewModel
import kotlinx.android.synthetic.main.activity_view_erc20.amount
import kotlinx.android.synthetic.main.activity_view_erc20.avatar
import kotlinx.android.synthetic.main.activity_view_erc20.closeButton
import kotlinx.android.synthetic.main.activity_view_erc20.fiat
import kotlinx.android.synthetic.main.activity_view_erc20.networkStatusView
import kotlinx.android.synthetic.main.activity_view_erc20.receive
import kotlinx.android.synthetic.main.activity_view_erc20.send
import kotlinx.android.synthetic.main.activity_view_erc20.toolbarTitle

class ViewTokenActivity : AppCompatActivity() {

    companion object {
        const val ETHER_TOKEN = "ether_token"
        const val ERC20_TOKEN = "erc_token"
        const val TOKEN_TYPE = "type"
    }

    private lateinit var viewModel: ViewTokenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_erc20)
        init()
    }

    private fun init() {
        val tokenType = intent.getStringExtra(TOKEN_TYPE)
        val token = when (tokenType) {
            ETHER_TOKEN -> EtherToken.getTokenFromIntent(intent)
            ERC20_TOKEN -> ERCTokenView.getTokenFromIntent(intent)
            else -> null
        }
        if (token == null) {
            toast(R.string.invalid_token)
            finish()
            return
        }
        initViewModel(token)
        initNetworkView()
        initObservers()
        initClickListeners()
    }

    private fun initViewModel(token: Token) {
        viewModel = getViewModel { ViewTokenViewModel(token) }
    }

    private fun initNetworkView() {
        networkStatusView.setNetworkVisibility(viewModel.getNetworks())
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
        send.setOnClickListener { startSendActivity() }
        receive.setOnClickListener { showShareWalletDialog() }
    }

    private fun startSendActivity() {
        val token = viewModel.token.value
        when (token) {
            is EtherToken -> startActivity<SendETHActivity> { EtherToken.buildIntent(this, token) }
            is ERCTokenView -> startActivity<SendERC20TokenActivity> { ERCTokenView.buildIntent(this, token) }
            else -> throw IllegalStateException(Throwable("Invalid token in this context"))
        }
    }

    private fun showShareWalletDialog() {
        ShareWalletAddressDialog.newInstance().apply {
            show(supportFragmentManager, ShareWalletAddressDialog.TAG)
        }
    }

    private fun initObservers() {
        viewModel.token.observe(this, Observer {
            if (it != null) updateUi(it)
        })
    }

    private fun updateUi(token: Token) {
        when (token) {
            is EtherToken -> renderEtherTokenUi(token)
            is ERCTokenView -> renderERCTokenUi(token)
        }
    }

    private fun renderEtherTokenUi(etherToken: EtherToken) {
        toolbarTitle.text = etherToken.name
        amount.text = getString(R.string.eth_balance, etherToken.etherValue)
        fiat.text = etherToken.fiatValue
        fiat.isVisible(true)
        avatar.setImageResource(etherToken.icon)
    }

    private fun renderERCTokenUi(ERCToken: ERCTokenView) {
        toolbarTitle.text = ERCToken.name
        ImageUtil.load(ERCToken.icon, avatar)
        val tokenAmount = TypeConverter.formatHexString(ERCToken.balance, ERCToken.decimals ?: 0, EthUtil.ETH_FORMAT)
        amount.text = "$tokenAmount ${ERCToken.symbol}"
    }
}