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

package com.toshi.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import com.toshi.R
import com.toshi.model.local.network.Networks
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import java.net.URI

class WebViewViewModel(startUrl: String) : ViewModel() {

    val addressBarUrl by lazy { MutableLiveData<String>() }
    val favicon by lazy { SingleLiveEvent<Bitmap>() }
    val title by lazy { SingleLiveEvent<String>() }
    val toolbarUpdate by lazy { SingleLiveEvent<Unit>() }
    val url by lazy { MutableLiveData<String>() }
    val mainFrameProgress by lazy { MutableLiveData<Int>() }

    init {
        setAddressBarUrl(startUrl) // AddressBar should contain url before loading starts
        url.value = startUrl
    }

    fun setAddressBarUrl(url: String) {
        if (url.startsWith("data:")) return
        addressBarUrl.value = url
    }

    fun tryGetAddress(): String {
        return try {
            getAddress()
        } catch (e: IllegalArgumentException) {
            BaseApplication.get().getString(R.string.unknown_address)
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun getAddress(): String {
        val value = url.value ?: throw IllegalArgumentException()
        val prefixedValue = prependScheme(value)
        val uri = URI.create(prefixedValue)
        return uri.toASCIIString()
    }

    private fun prependScheme(value: String): String {
        return if (value.startsWith("http")) value else "http://$value"
    }

    fun updateToolbar() = toolbarUpdate.postValue(Unit)

    fun getNetworks() = Networks.getInstance()
}