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

import android.util.Patterns
import com.toshi.extensions.USERNAME_PATTERN

open class ChatKeywordFinder {
    companion object {
        fun findKeywords(text: String, pattern: String): List<Keyword> {
            val regexResult = Regex(pattern).findAll(text)
            val matches = mutableListOf<Keyword>()
            regexResult.forEach {
                val keyword = it.value
                val keywordType = when {
                    isWebUrl(keyword) -> KeywordType.WEB_URL
                    isUsername(keyword) -> KeywordType.USERNAME
                    else -> null
                }
                matches.add(Keyword(keyword, keywordType ?: return@forEach))
            }
            return matches
        }

        private fun isWebUrl(text: String): Boolean {
            val regexResult = Regex(getWebUrlPattern()).find(text)
            return regexResult?.value === text
        }

        private fun isUsername(text: String): Boolean {
            val regexResult = Regex(getUsernamePattern()).find(text)
            return regexResult?.value === text
        }

        private fun getWebUrlPattern(): String = Patterns.WEB_URL.pattern()
        private fun getUsernamePattern(): String = USERNAME_PATTERN
        fun getKeywordPattern(): String = "${getUsernamePattern()}|${getWebUrlPattern()}"
    }
}

data class Keyword(
        val text: String,
        val type: KeywordType
)

enum class KeywordType {
    WEB_URL,
    USERNAME
}