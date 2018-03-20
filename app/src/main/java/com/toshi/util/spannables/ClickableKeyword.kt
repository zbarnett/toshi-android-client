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

package com.toshi.util.spannables

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.toshi.util.SingleClickableSpan
import com.toshi.view.adapter.listeners.OnItemClickListener

class ClickableSpanUtil {
    companion object {
        fun addClickableKeywords(text: String,
                                 textView: TextView,
                                 webUrlListener: OnItemClickListener<String>,
                                 usernameListener: OnItemClickListener<String>) {
            val spannableString = SpannableString(text)
            var lastEndPos = 0
            val keywordPattern = ChatKeywordFinder.getKeywordPattern()
            val keywords = ChatKeywordFinder.findKeywords(text, keywordPattern)

            for (keyword in keywords) {
                val currentStartPos = text.indexOf(keyword.text, lastEndPos)
                val currentEndPos = text.indexOf(keyword.text, lastEndPos) + keyword.text.length

                spannableString.setSpan(object : SingleClickableSpan() {
                    override fun onSingleClick(view: View) {
                        handleKeywordClicked(
                                view = view,
                                keyword = keyword,
                                clickableSpan = this,
                                webUrlListener = webUrlListener,
                                usernameListener = usernameListener
                        )
                    }
                },
                        currentStartPos,
                        currentEndPos,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )

                lastEndPos = currentEndPos
            }

            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        private fun handleKeywordClicked(view: View,
                                         clickableSpan: SingleClickableSpan,
                                         keyword: Keyword,
                                         webUrlListener: OnItemClickListener<String>,
                                         usernameListener: OnItemClickListener<String>) {
            if (view !is TextView) return
            when (keyword.type) {
                KeywordType.WEB_URL -> handleWebUrlClicked(view, clickableSpan, webUrlListener)
                KeywordType.USERNAME -> handleUsernameClicked(view, clickableSpan, usernameListener)
            }
        }

        private fun handleUsernameClicked(view: TextView, clickableSpan: SingleClickableSpan, listener: OnItemClickListener<String>) {
            val spannedString = view.text as Spanned
            val username = spannedString.subSequence(
                    spannedString.getSpanStart(clickableSpan),
                    spannedString.getSpanEnd(clickableSpan)
            ).toString().substring(1)
            listener.onItemClick(username)
        }

        private fun handleWebUrlClicked(view: TextView, clickableSpan: SingleClickableSpan, listener: OnItemClickListener<String>) {
            val spannedString = view.text as Spanned
            val username = spannedString.subSequence(
                    spannedString.getSpanStart(clickableSpan),
                    spannedString.getSpanEnd(clickableSpan)
            ).toString()
            listener.onItemClick(username)
        }
    }
}