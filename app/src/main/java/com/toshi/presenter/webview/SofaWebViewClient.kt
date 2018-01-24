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

package com.toshi.presenter.webview

import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.toshi.R
import com.toshi.view.BaseApplication
import com.toshi.view.custom.listener.OnLoadListener
import java.net.MalformedURLException
import java.net.URL

class SofaWebViewClient(private val listener: OnLoadListener) : WebViewClient() {

    private val urls by lazy { mutableListOf<String>() }
    private var lastIndex = 0

    override fun shouldOverrideUrlLoading(webView: WebView?, request: WebResourceRequest?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listener.newPageLoad(request?.url.toString())
        }
        return true
    }

    override fun shouldOverrideUrlLoading(webView: WebView?, newUrl: String?): Boolean {
        listener.newPageLoad(newUrl)
        return true
    }

    override fun onPageCommitVisible(webView: WebView?, newUrl: String?) = listener.onLoaded()

    override fun onPageStarted(webView: WebView?, newUrl: String?, favicon: Bitmap?) {
        val baseUrl = getSafeBaseUrl(newUrl)
        val title = webView?.title ?: getDefaultTitle()
        listener.updateUrl(baseUrl)
        listener.updateTitle(title)
    }

    override fun onPageFinished(webView: WebView?, newUrl: String?) {
        val currentIndex = getCurrentIndex(webView)
        val baseUrl = addOrRemoveUrl(currentIndex, newUrl)
        val title = webView?.title ?: getDefaultTitle()
        listener.updateUrl(baseUrl)
        listener.updateTitle(title)
        listener.onLoaded()
        updateLastIndex(webView)
    }

    private fun getDefaultTitle() = BaseApplication.get().getString(R.string.untitled)

    private fun addOrRemoveUrl(currentIndex: Int, url: String?): String {
        val isGoingBack = currentIndex < lastIndex
        if (isGoingBack) urls.removeAt(urls.lastIndex)
        val baseUrl = getSafeBaseUrl(url)
        if (!isGoingBack) urls.add(baseUrl)
        return baseUrl
    }

    private fun getCurrentIndex(webView: WebView?) = webView?.copyBackForwardList()?.currentIndex ?: 0

    private fun updateLastIndex(webView: WebView?) {
        lastIndex = webView?.copyBackForwardList()?.currentIndex ?: 0
    }

    private fun getSafeBaseUrl(address: String?): String {
        val baseUrl = getBaseUrl(address)
        return if (urls.isEmpty()) baseUrl ?: BaseApplication.get().getString(R.string.unknown_address)
        else baseUrl ?: urls[urls.lastIndex] // Return the last url if the new url is null
    }

    private fun getBaseUrl(newUrl: String?): String? {
        return try {
            val url = URL(newUrl)
            "${url.protocol}://${url.host}"
        } catch (e: MalformedURLException) {
            null
        }
    }
}