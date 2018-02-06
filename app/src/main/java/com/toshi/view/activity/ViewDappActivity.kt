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

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.extensions.startActivity
import com.toshi.util.ImageUtil
import com.toshi.view.activity.webView.JellyBeanWebViewActivity
import com.toshi.view.activity.webView.LollipopWebViewActivity
import kotlinx.android.synthetic.main.activity_view_dapp.*

class ViewDappActivity : AppCompatActivity() {

    companion object {
        const val EXTRA__DAPP_ADDRESS = "extra_dapp_address"
        const val EXTRA__DAPP_NAME = "extra_dapp_name"
        const val EXTRA__DAPP_AVATAR = "extra_dapp_avatar"
        const val EXTRA__DAPP_ABOUT = "extra_dapp_about"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_dapp)
        init()
    }

    private fun init() {
        initToolbar()
        initClickListeners()
        initUi()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { onBackPressed() }
        enter.setOnClickListener { startWebViewActivity() }
    }

    private fun startWebViewActivity() {
        if (Build.VERSION.SDK_INT >= 21) {
            startActivity<LollipopWebViewActivity> {
                putExtra(LollipopWebViewActivity.EXTRA__ADDRESS, getAddressFromIntent())
            }
        } else {
            startActivity<JellyBeanWebViewActivity> {
                putExtra(JellyBeanWebViewActivity.EXTRA__ADDRESS, getAddressFromIntent())
            }
        }
    }

    private fun initUi() {
        toolbar.title = getNameFromIntent()
        aboutUser.text = getAboutFromIntent()
        address.text = getAddressFromIntent()
        ImageUtil.load(getAvatarFromIntent(), avatar)
    }

    private fun getAddressFromIntent() = intent.getStringExtra(EXTRA__DAPP_ADDRESS)

    private fun getNameFromIntent() = intent.getStringExtra(ViewDappActivity.EXTRA__DAPP_NAME)

    private fun getAboutFromIntent() = intent.getStringExtra(ViewDappActivity.EXTRA__DAPP_ABOUT)

    private fun getAvatarFromIntent() = intent.getStringExtra(ViewDappActivity.EXTRA__DAPP_AVATAR)
}
