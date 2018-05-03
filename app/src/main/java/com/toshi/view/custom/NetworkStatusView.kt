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

package com.toshi.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import com.toshi.R
import com.toshi.extensions.getColorById
import com.toshi.extensions.getPxSize
import com.toshi.extensions.getString
import com.toshi.extensions.isVisible
import com.toshi.model.local.network.Network
import com.toshi.model.local.network.Networks

class NetworkStatusView : TextView {
    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int): super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        setTextColor(getColorById(R.color.textColorContrast))
        setBackgroundColor(getColorById(R.color.network_status_background))
        gravity = Gravity.CENTER_VERTICAL
        val padding = getPxSize(R.dimen.margin_primary)
        setPadding(padding, 0, padding, 0)
        isVisible(false)
    }

    fun setNetworkVisibility(networks: Networks) {
        setVisibilty(networks)
        setNetworkName(networks.currentNetwork)
    }

    private fun setVisibilty(networks: Networks) {
        val isDefaultNetwork = networks.onDefaultNetwork()
        isVisible(!isDefaultNetwork)
    }

    private fun setNetworkName(currentNetwork: Network) {
        val networkName = getString(R.string.network_name, currentNetwork.name)
        text = networkName
    }
}