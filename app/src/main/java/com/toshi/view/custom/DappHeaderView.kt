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

package com.toshi.view.custom

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.util.AttributeSet
import com.toshi.R
import kotlinx.android.synthetic.main.view_dapp_header.view.headerImage

class DappHeaderView : AppBarLayout {

    private var prevOffset = -1

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.view_dapp_header, this)
        initListeners()
    }

    private fun initListeners() {
        addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            updateView(verticalOffset, appBarLayout)
        }
    }

    private fun updateView(verticalOffset: Int, appBarLayout: AppBarLayout) {
        if (prevOffset == verticalOffset) return
        prevOffset = verticalOffset
        val absVerticalOffset = Math.abs(verticalOffset).toFloat()
        val scrollRange = appBarLayout.totalScrollRange.toFloat()
        val percentage = absVerticalOffset / scrollRange
        setHeaderImageAlpha(percentage)
    }

    private fun setHeaderImageAlpha(percentage: Float) {
        headerImage.alpha = 1f - percentage
    }
}