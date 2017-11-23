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

package com.toshi.view.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.toshi.R
import kotlinx.android.synthetic.main.view_conversation_request.view.*

class ConversationRequestView : LinearLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    var onAcceptClickListener: (() -> Unit)? = null
    var onDeclineClickListener: (() -> Unit)? = null
    var onSizeChangedListener: ((Int) -> Unit)? = null

    private fun init() {
        val view = inflate(context, R.layout.view_conversation_request, this)
        view.accept.setOnClickListener { onAcceptClickListener?.invoke() }
        view.decline.setOnClickListener { onDeclineClickListener?.invoke() }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (height == 0) return
        onSizeChangedListener?.invoke(height)
    }
}