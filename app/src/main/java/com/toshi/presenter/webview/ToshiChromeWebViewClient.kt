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
import android.net.Uri
import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.toshi.BuildConfig
import com.toshi.util.logging.LogUtil

class ToshiChromeWebViewClient : WebChromeClient() {

    var onOpenFilePickerListener: ((ValueCallback<Array<Uri>>?) -> Boolean)? = null
    var onProgressChangedListener: ((Int) -> Unit)? = null
    var onTitleReceivedListener: ((String) -> Unit)? = null
    var onIconReceivedListener: ((Bitmap) -> Unit)? = null

    override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?): Boolean {
        return onOpenFilePickerListener?.invoke(filePathCallback) ?: false
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        onProgressChangedListener?.invoke(newProgress)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT <= 18) {
            LogUtil.d("WebView Console: ${consoleMessage?.message()}")
        }
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        if (title != null) onTitleReceivedListener?.invoke(title)
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        if (icon != null) onIconReceivedListener?.invoke(icon)
    }
}