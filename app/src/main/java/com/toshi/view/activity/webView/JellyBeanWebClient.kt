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

package com.toshi.view.activity.webView

import android.content.Context
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.toshi.R
import com.toshi.util.logging.LogUtil
import com.toshi.util.webView.WebViewCookieJar
import com.toshi.view.BaseApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

class JellyBeanWebClient(
        private val context: Context,
        private val updateListener: () -> Unit,
        private val onUrlAvailable: (String) -> Unit,
        private val onSofaInjectResponse: (SofaInjectResponse) -> Unit,
        private val pageCommitVisibleListener: (String?) -> Unit
) : WebViewClient() {

    private var lastIndex = -1
    private val urls by lazy { mutableListOf<String>() }
    private val subscriptions by lazy { CompositeSubscription() }

    private val toshiManager by lazy { BaseApplication.get().toshiManager }
    private val httpClient by lazy { OkHttpClient.Builder().cookieJar(WebViewCookieJar()).build() }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            request?.url?.let { newPageLoad(it.toString()) }
        }
        return true
    }

    override fun shouldOverrideUrlLoading(webView: WebView?, newUrl: String?): Boolean {
        if (newUrl != null) newPageLoad(newUrl)
        return true
    }

    fun newPageLoad(newUrl: String) {
        val sub = loadPage(newUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSofaInjectResponse(it) },
                        { LogUtil.exception(it) }
                )
        subscriptions.add(sub)
    }

    private fun loadPage(newUrl: String): Single<SofaInjectResponse> {
        return Single.fromCallable { loadAndInjectSofa(newUrl) }
    }

    @Throws(IOException::class)
    private fun loadAndInjectSofa(url: String): SofaInjectResponse {
        val request = Request.Builder()
                .url(url)
                .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body()?.string()
        if (!response.isSuccessful || body == null) {
            throw IOException("Unexpected code " + response)
        }

        val injectedBody = injectSofaScript(body)

        return SofaInjectResponse.Builder()
                .setAddress(response.request().url().toString())
                .setData(injectedBody)
                .setMimeType(response.header("Content-Type", "text/html; charset=utf-8"))
                .setEncoding(response.header("Content-Encoding", "UTF-8"))
                .build()
    }

    private fun injectSofaScript(body: String): String {
        val position = getInjectionPosition(body)
        if (position == -1) return body
        val script = loadInjections()
        return injectScripts(body, script)
    }

    private fun loadInjections(): String {
        return loadWebViewSupportInjections() + loadSofaScript()
    }

    private fun loadSofaScript(): String {
        val sb = StringBuilder()
        val rcpUrl = "window.SOFA = {" +
                "config: {accounts: [\"${getWallet().paymentAddress}\"]," +
                "rcpUrl: \"${context.getString(R.string.rcp_url)}\"}" +
                "};"
        sb.append(rcpUrl)
        val stream = context.resources.openRawResource(R.raw.sofa)
        val reader = BufferedReader(InputStreamReader(stream))
        val text: List<String> = reader.readLines()
        for (line in text) sb.append(line).append("\n")
        return "<script type=\"text/javascript\">$sb</script>"
    }

    private fun loadWebViewSupportInjections(): String {
        val sb = StringBuilder()
        val supportInjection = loadInjection(R.raw.webviewsupport)
        sb.append(supportInjection)
        if (Build.VERSION.SDK_INT <= 18) {
            val supportXMLHttpRequestInjection = loadInjection(R.raw.webviewsupport18)
            sb.append(supportXMLHttpRequestInjection)
        }
        return "<script type=\"text/javascript\">$sb</script>"
    }

    private fun loadInjection(resourceId: Int): StringBuilder {
        val sb = StringBuilder()
        val stream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(stream))
        val text: List<String> = reader.readLines()
        for (line in text) sb.append(line).append("\n")
        return sb
    }

    private fun getWallet() = toshiManager.wallet.toBlocking().value()

    private fun injectScripts(body: String?, script: String): String {
        val safeBody = body.orEmpty()
        val position = getInjectionPosition(safeBody)
        if (position == -1) return safeBody
        val beforeTag = safeBody.substring(0, position)
        val afterTab = safeBody.substring(position)
        return beforeTag + script + afterTab
    }

    private fun getInjectionPosition(body: String): Int {
        val ieDetectTagIndex = body.indexOf("<!--[if", 0, true)
        val scriptTagIndex = body.indexOf("<script", 0, true)
        return if (ieDetectTagIndex < 0) scriptTagIndex else minOf(scriptTagIndex, ieDetectTagIndex)
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        pageCommitVisibleListener(url)
    }

    override fun onPageFinished(webView: WebView?, newUrl: String?) {
        val currentIndex = getCurrentIndex(webView)
        addOrRemoveUrl(currentIndex, newUrl)
        updateLastIndex(webView)
        if (newUrl != null) onUrlAvailable(newUrl)
        updateListener()
    }

    private fun getCurrentIndex(webView: WebView?) = webView?.copyBackForwardList()?.currentIndex ?: 0

    private fun updateLastIndex(webView: WebView?) {
        lastIndex = webView?.copyBackForwardList()?.currentIndex ?: 0
    }

    private fun addOrRemoveUrl(currentIndex: Int, url: String?): String {
        val isGoingBack = currentIndex < lastIndex
        if (isGoingBack) urls.removeAt(urls.lastIndex)
        val baseUrl = getSafeBaseUrl(url)
        if (!isGoingBack) urls.add(baseUrl)
        return baseUrl
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

    fun clear() = subscriptions.clear()
}