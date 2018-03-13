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
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.toshi.R

class CategoryList : FlexboxLayout {
    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    var itemClickedListener: ((Int, String) -> Unit)? = null

    fun init() {
        inflate(context, R.layout.view_category_list, this)
    }

    fun addCategories(categories: Map<Int, String>) {
        removeAllViews()
        val sortedMap = categories.toSortedMap()
        sortedMap.forEach {
            val renderComma = it.key != sortedMap.lastKey()
            addTextView(it.key, it.value, renderComma)
        }
    }

    private fun addTextView(key: Int, value: String, renderComma: Boolean) {
        val textView = inflate(context, R.layout.view_category__item, null) as TextView
        textView.text = if (renderComma) "$value, " else value
        textView.setOnClickListener { itemClickedListener?.invoke(key, (it as TextView).text.toString()) }
        addView(textView)
    }
}