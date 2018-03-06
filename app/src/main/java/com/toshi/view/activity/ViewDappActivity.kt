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

import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.extensions.getAbsoluteY
import kotlinx.android.synthetic.main.activity_view_dapp.header
import kotlinx.android.synthetic.main.activity_view_dapp.name
import kotlinx.android.synthetic.main.activity_view_dapp.scrollView
import kotlinx.android.synthetic.main.view_dapp_header.toolbarTitle
import kotlinx.android.synthetic.main.view_dapp_header.view.toolbar
import kotlinx.android.synthetic.main.view_dapp_header.view.toolbarTitle

class ViewDappActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_dapp)
        init()
    }

    private fun init() {
        scrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            setToolbarTitleAlpha()
            setYOfToolbarTitle()
        }
    }

    private fun setToolbarTitleAlpha() {
        val nameTop = getAbsoluteY(name) - getStartY()
        val minY = (header.toolbar.height / 2) - (toolbarTitle.height / 2)
        val toolbarY = header.toolbar.height - minY
        val alpha = 1 - nameTop.toDouble() / toolbarY.toDouble()
        val safeAlpha = if (alpha < 0) 0.0 else if (alpha > 1) 1.0 else alpha
        toolbarTitle.alpha = safeAlpha.toFloat()
    }

    private fun setYOfToolbarTitle() {
        val nameTop = getAbsoluteY(name) - getStartY()
        val toolbarTitle = header.toolbar.toolbarTitle
        val minY = (header.toolbar.height / 2) - (toolbarTitle.height / 2)
        if (nameTop < minY) toolbarTitle.y = minY.toFloat()
        else toolbarTitle.y = nameTop.toFloat()
    }

    private fun getStartY() = getAbsoluteY(header.toolbar)
}
