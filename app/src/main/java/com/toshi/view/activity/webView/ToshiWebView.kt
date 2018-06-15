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
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.support.v4.view.GestureDetectorCompat

class ToshiWebView : WebView {

    var toshiWebClient: ToshiWebClient? = null
    var onReloadListener: (() -> Unit)? = null
    var canScrollUp = false
        private set
    private val gestureDetector: GestureDetectorCompat

    constructor(context: Context) : super(context) {
        gestureDetector = GestureDetectorCompat(context, ScrollGestureListener());
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        gestureDetector = GestureDetectorCompat(context, ScrollGestureListener());
    }
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        gestureDetector = GestureDetectorCompat(context, ScrollGestureListener());
    }

    override fun setWebViewClient(client: WebViewClient?) {
        super.setWebViewClient(client)
        toshiWebClient = client as? ToshiWebClient
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    inner class ScrollGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(start: MotionEvent?, end: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            if (distanceY > 0) canScrollUp = true
            return false
        }
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        if (clampedY && scrollY == 0) canScrollUp = false
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }

    override fun reload() = onReloadListener?.invoke() ?: super.reload()

    override fun destroy() {
        toshiWebClient?.destroy()
        toshiWebClient = null
        super.destroy()
    }
}
