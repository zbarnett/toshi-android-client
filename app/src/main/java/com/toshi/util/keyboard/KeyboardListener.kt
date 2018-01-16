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

package com.toshi.util.keyboard

import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.toshi.R
import com.toshi.extensions.getPxSize

class KeyboardListener(
        private val activity: AppCompatActivity,
        private val onKeyboardVisible: () -> Unit
) {

    private val layoutListener: ViewTreeObserver.OnGlobalLayoutListener

    init {
        layoutListener = initKeyboardListener()
    }

    private fun initKeyboardListener(): ViewTreeObserver.OnGlobalLayoutListener {
        val rect = Rect()
        val threshold = activity.getPxSize(R.dimen.keyboard_threshold)
        val activityRoot = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        var wasOpened = false

        val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            activityRoot.getWindowVisibleDisplayFrame(rect)
            val heightDiff = activityRoot.rootView.height - rect.height()
            val isOpen = heightDiff > threshold
            if (isOpen == wasOpened) return@OnGlobalLayoutListener
            wasOpened = isOpen
            if (isOpen) onKeyboardVisible()
        }

        activityRoot?.viewTreeObserver?.addOnGlobalLayoutListener(layoutListener)
        return layoutListener
    }

    fun clear() {
        val activityRoot = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        activityRoot?.viewTreeObserver?.removeOnGlobalLayoutListener { layoutListener }
    }
}