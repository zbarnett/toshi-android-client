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

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.toshi.R
import com.toshi.model.local.network.Networks
import com.toshi.util.webView.WebViewCookieJar
import com.toshi.view.BaseApplication
import okhttp3.Interceptor.Chain
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLPeerUnverifiedException

class ToshiWebClient(
        private val context: Context,
        private val userAgentString: String
) : WebViewClient() {

    companion object {
        const val HEADER_USER_AGENT = "User-Agent"
    }

    private val subscriptions by lazy { CompositeSubscription() }
    private var temporaryResponse: WebResourceResponse? = null

    var onHistoryUpdatedListener: (() -> Unit)? = null
    var onUrlUpdatedListener: ((String) -> Unit)? = null
    var onPageLoadingStartedListener: (() -> Unit)? = null
    var onPageLoadedListener: ((String?) -> Unit)? = null
    var onOverrideWebViewAddressListener: ((String) -> Unit)? = null
    var onMainFrameProgressChangedListener: ((Int) -> Unit)? = null

    private val toshiManager by lazy { BaseApplication.get().toshiManager }
    private val httpClient by lazy { initOkHttpClient() }

    private fun initOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .cookieJar(WebViewCookieJar())
                .followRedirects(true)
                .followSslRedirects(true)
                .addNetworkInterceptor { interceptOkHttpRequest(it) }
                .build()
    }

    private fun interceptOkHttpRequest(chain: Chain): Response? {
        val response = chain.proceed(chain.request())
        val responseBody = response.body()
        return if (responseBody == null) {
            null
        } else {
            buildProgressResponseInterceptor(response, responseBody)
        }
    }

    private fun buildProgressResponseInterceptor(response: Response, responseBody: ResponseBody): Response? {
        return response.newBuilder()
                .body(ProgressResponseBody(responseBody, {
                    onMainFrameProgressChangedListener?.invoke(it)
                }))
                .build()
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageLoadingStartedListener?.invoke()
    }

    @TargetApi(21)
    override fun shouldInterceptRequest(view: WebView?, webRequest: WebResourceRequest?): WebResourceResponse? {
        return when {
            webRequest?.method != "GET" || !webRequest.isForMainFrame -> null
            else -> interceptRequest(webRequest)
        }
    }

    @TargetApi(21)
    private fun interceptRequest(webRequest: WebResourceRequest): WebResourceResponse? {
        val request = buildRequest(webRequest)
        val address = webRequest.url.toString()
        if (address.startsWith("data:")) return null
        if (request == null) return null
        return try {
            if (temporaryResponse != null) {
                val response = temporaryResponse
                temporaryResponse = null
                response
            } else {
                val response = httpClient.newCall(request).execute()
                val body = response.body()?.string() ?: ""
                val injectedBody = injectBody(body)
                buildWebResponse(response, injectedBody)
            }
        } catch (e: SSLPeerUnverifiedException) {
            null
        } catch (e: UnknownHostException) {
            null
        } catch (e: ConnectException) {
            null
        } catch (e: SocketTimeoutException) {
            null
        }
    }

    @TargetApi(21)
    private fun buildRequest(webRequest: WebResourceRequest): Request? {
        return try {
            val requestBuilder = Request.Builder()
                    .header(HEADER_USER_AGENT, userAgentString)
                    .get()
                    .url(webRequest.url.toString())
            webRequest.requestHeaders.forEach {
                requestBuilder.addHeader(it.key, it.value)
            }
            requestBuilder.build()
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun buildWebResponse(response: Response, body: String): WebResourceResponse? {
        val byteStream = ByteArrayInputStream(body.toByteArray())
        val headerParser = HeaderParser()
        val contentType = headerParser.getContentTypeHeader(response)
        val charset = headerParser.getCharset(contentType)
        val mimeType = headerParser.getMimeType(contentType)
        return WebResourceResponse(mimeType, charset, byteStream)
    }

    private fun injectBody(body: String): String {
        val script = loadInjections()
        return injectScripts(body, script)
    }

    private fun loadInjections(): String {
        return if (Build.VERSION.SDK_INT >= 24) {
            loadSofaScript()
        } else {
            loadWebViewSupportInjection() + loadSofaScript()
        }
    }

    private fun loadSofaScript(): String {
        val sb = StringBuilder()
        val networkId = Networks.getInstance().currentNetwork.id
        val rcpUrl = "window.SOFA = {" +
                "config: {netVersion: \"$networkId\", " +
                "accounts: [\"${getWallet().paymentAddress}\"]," +
                "rcpUrl: \"${context.getString(R.string.rcp_url)}\"}" +
                "};"
        sb.append(rcpUrl)
        val stream = context.resources.openRawResource(R.raw.sofa)
        val reader = BufferedReader(InputStreamReader(stream))
        val text: List<String> = reader.readLines()
        for (line in text) sb.append(line).append("\n")
        return "<script type=\"text/javascript\">$sb</script>"
    }

    private fun loadWebViewSupportInjection(): String {
        val sb = StringBuilder()
        val stream = context.resources.openRawResource(R.raw.webviewsupport)
        val reader = BufferedReader(InputStreamReader(stream))
        val text: List<String> = reader.readLines()
        for (line in text) sb.append(line).append("\n")
        return "<script type=\"text/javascript\">$sb</script>"
    }

    private fun getWallet() = toshiManager.getWallet().toBlocking().value()

    private fun injectScripts(body: String?, script: String): String {
        val safeBody = body ?: ""
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
        onPageLoadedListener?.invoke(url) // Build.VERSION.SDK_INT >= 23
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        if (Build.VERSION.SDK_INT < 23) onPageLoadedListener?.invoke(url)
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        if (!isReload) onHistoryUpdatedListener?.invoke()
    }

    fun loadUrl(address: String, webview: WebView?) {
        if (webview == null) return
        val subscription = loadAndInject(address, webview.url ?: "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { handlePreloadedResponse(webview, it.first, it.second) },
                        { webview.loadUrl(address) }
                )
        subscriptions.add(subscription)
    }

    private fun loadAndInject(address: String, currentUrl: String): Single<Pair<String, String?>> {
        return Single.fromCallable { sendRequest(address) }
                .map { handleResponse(it, currentUrl) }
                .subscribeOn(Schedulers.io())
    }

    private fun sendRequest(url: String?): Response {
        val requestBuilder = Request.Builder()
                .header(HEADER_USER_AGENT, userAgentString)
                .get()
                .url(url)
        val request = requestBuilder.build()
        return httpClient.newCall(request).execute()
    }

    private fun handleResponse(response: Response, currentUrl: String): Pair<String, String?> {
        val finalUrl = response.request()?.url()?.toString() ?: "about:blank"
        val body = response.body()?.string() ?: ""
        val injectedBody = injectBody(body)
        val returnedBody = if (currentUrl == finalUrl) {
            injectedBody
        } else {
            temporaryResponse = buildWebResponse(response, injectedBody)
            null
        }
        return Pair(finalUrl, returnedBody)
    }

    private fun handlePreloadedResponse(webView: WebView, url: String, body: String?) {
        if (body == null) webView.loadUrl(url)
        else setWebViewContent(body, webView, url)
    }

    private fun setWebViewContent(body: String, webView: WebView, finalAddress: String) {
        onOverrideWebViewAddressListener?.invoke(finalAddress)
        webView.loadDataWithBaseURL(
                finalAddress,
                body,
                null,
                null,
                finalAddress
        )
    }

    fun destroy() = subscriptions.clear()
}