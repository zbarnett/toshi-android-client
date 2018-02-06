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

import okhttp3.Response

class HeaderParser {

    companion object {
        const val DEFAULT_CHARSET = "text/html"
        const val DEFAULT_MIME_TYPE = "utf-8"
    }

    fun getMimeType(contentType: String): String {
        val regexResult = Regex("^.*(?=;)").find(contentType)
        return regexResult?.value ?: DEFAULT_MIME_TYPE
    }

    fun getCharset(contentType: String): String {
        val regexResult = Regex(
                "charset=([a-zA-Z0-9-]+)",
                RegexOption.IGNORE_CASE
        ).find(contentType)
        val groupValues = regexResult?.groupValues ?: return DEFAULT_CHARSET
        return if (groupValues.size != 2) DEFAULT_CHARSET
        else regexResult.groupValues[1]
    }

    fun getContentTypeHeader(response: Response): String {
        val headers = response.headers()
        val contentType = headers.get("Content-Type")
                ?: headers.get("content-Type")
                ?: "text/html; charset=utf-8"
        contentType.trim()
        return contentType
    }
}