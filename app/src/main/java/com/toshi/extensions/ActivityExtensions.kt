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
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.view.WindowManager
import android.widget.Toast

fun AppCompatActivity.getColorById(@ColorRes id: Int) = ContextCompat.getColor(this, id)

inline fun <reified T> AppCompatActivity.startActivity() = startActivity(Intent(this, T::class.java))

fun AppCompatActivity.toast(@StringRes id: Int, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, id, duration).show()

inline fun <reified T> AppCompatActivity.startActivity(func: Intent.() -> Intent) = startActivity(Intent(this, T::class.java).func())

inline fun <reified T> AppCompatActivity.startActivityAndFinish(func: Intent.() -> Intent) {
    startActivity(Intent(this, T::class.java).func())
    finish()
}

inline fun <reified T> AppCompatActivity.startActivityAndFinish() {
    startActivity(Intent(this, T::class.java))
    finish()
}

inline fun <reified T> AppCompatActivity.startActivityForResult(requestCode: Int, func: Intent.() -> Intent) {
    startActivityForResult(Intent(this, T::class.java).func(), requestCode)
}

inline fun AppCompatActivity.setActivityResultAndFinish(resultCode: Int, func: Intent.() -> Intent) {
    setResult(resultCode, intent.func())
    finish()
}

fun AppCompatActivity.getDrawableById(@DrawableRes id: Int) = AppCompatResources.getDrawable(this, id)

fun AppCompatActivity.hideStatusBar() = window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

fun AppCompatActivity.getPxSize(@DimenRes id: Int) = resources.getDimensionPixelSize(id)