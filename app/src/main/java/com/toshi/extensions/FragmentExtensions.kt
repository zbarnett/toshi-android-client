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

package com.toshi.extensions

import android.content.Intent
import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Patterns
import android.widget.Toast
import com.toshi.view.activity.webView.JellyBeanWebViewActivity
import com.toshi.view.activity.webView.LollipopWebViewActivity

inline fun <reified T> Fragment.startActivity(func: Intent.() -> Intent) = startActivity(Intent(activity, T::class.java).func())

inline fun <reified T> Fragment.startActivityForResult(requestCode: Int, func: Intent.() -> Intent) {
    val intent = Intent(activity, T::class.java).func()
    startActivityForResult(intent, requestCode)
}

inline fun <reified T> Fragment.startActivity() = startActivity(Intent(activity, T::class.java))

fun Fragment.getPxSize(@DimenRes id: Int) = resources.getDimensionPixelSize(id)

fun Fragment.getColorById(@ColorRes id: Int) = context?.let { ContextCompat.getColor(it, id) }

fun Fragment.startExternalActivity(func: Intent.() -> Intent) = startActivity(Intent().func(), null)

inline fun <reified T> Fragment.startActivityAndFinish() {
    startActivity(Intent(activity, T::class.java))
    activity?.finish()
}

fun Fragment.toast(@StringRes id: Int, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(context, id, duration).show()

fun Fragment.getColor(@ColorRes id: Int) = context?.let { ContextCompat.getColor(it, id) }

fun Fragment.isWebUrl(value: String) = Patterns.WEB_URL.matcher(value.trim()).matches()

fun Fragment.openWebView(address: String) {
    if (Build.VERSION.SDK_INT >= 21) {
        startActivity<LollipopWebViewActivity> {
            putExtra(LollipopWebViewActivity.EXTRA__ADDRESS, address)
        }
    } else {
        startActivity<JellyBeanWebViewActivity> {
            putExtra(JellyBeanWebViewActivity.EXTRA__ADDRESS, address)
        }
    }
}

fun Fragment.openWebViewForResult(requestCode: Int, address: String) {
    if (Build.VERSION.SDK_INT >= 21) {
        startActivityForResult<LollipopWebViewActivity>(requestCode) {
            putExtra(LollipopWebViewActivity.EXTRA__ADDRESS, address)
            putExtra(LollipopWebViewActivity.EXIT_ACTION, true)
        }
    } else {
        startActivity<JellyBeanWebViewActivity> {
            putExtra(JellyBeanWebViewActivity.EXTRA__ADDRESS, address)
        }
    }
}