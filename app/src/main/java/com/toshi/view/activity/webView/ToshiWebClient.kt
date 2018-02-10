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
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.toshi.R
import com.toshi.util.webView.WebViewCookieJar
import com.toshi.view.BaseApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class ToshiWebClient(
        private val context: Context,
        private val updateListener: () -> Unit
) : WebViewClient() {

    private val toshiManager by lazy { BaseApplication.get().toshiManager }
    private val httpClient by lazy { OkHttpClient.Builder().cookieJar(WebViewCookieJar()).build() }

    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        return null
    }

    @TargetApi(21)
    override fun shouldInterceptRequest(view: WebView?, webRequest: WebResourceRequest?): WebResourceResponse? {
        if (webRequest?.method != "GET") return null
        updateListener()
        if (!webRequest.isForMainFrame) return null
        return interceptRequest(webRequest)
    }

    @TargetApi(21)
    private fun interceptRequest(webRequest: WebResourceRequest): WebResourceResponse? {
        val request = buildRequest(webRequest)
        val response = httpClient.newCall(request).execute()
        return if (response.priorResponse()?.isRedirect == true) null
        else buildWebResponse(response)
    }

    @TargetApi(21)
    private fun buildRequest(webRequest: WebResourceRequest): Request {
        val requestBuilder = Request.Builder()
                .get()
                .url(webRequest.url.toString())
        webRequest.requestHeaders.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        return requestBuilder.build()
    }

    private fun buildWebResponse(response: Response): WebResourceResponse? {
        val body = response.body()?.string() ?: ""
        val script = loadSofaScript()
        val injectedBody = injectScripts(body, script)
        val byteStream = ByteArrayInputStream(injectedBody.toByteArray())
        val headerParser = HeaderParser()
        val contentType = headerParser.getContentTypeHeader(response)
        val charset = headerParser.getCharset(contentType)
        val mimeType = headerParser.getMimeType(contentType)
        return WebResourceResponse(mimeType, charset, byteStream)
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

    private fun getWallet() = toshiManager.wallet.toBlocking().value()

    private fun injectScripts(body: String?, script: String): String {
        val safeBody = body ?: ""
        val position = getInjectionPosition(safeBody)
        if (position == -1) return safeBody
        val beforeTag = safeBody.substring(0, position)
        val afterTab = safeBody.substring(position)
        return beforeTag + script + afterTab
    }

    private fun getInjectionPosition(body: String): Int {
        val htmlTag = "<script"
        return body.toLowerCase().indexOf(htmlTag)
    }
}