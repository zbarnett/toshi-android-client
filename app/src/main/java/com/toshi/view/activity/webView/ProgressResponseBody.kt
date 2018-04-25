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

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.Okio
import okio.Source
import okio.ForwardingSource
import okio.Buffer

class ProgressResponseBody(
        private val responseBody: ResponseBody,
        private val progressListener: (Int) -> Unit
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentLength(): Long = responseBody.contentLength()

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun source(): BufferedSource? {
        if (bufferedSource == null) bufferedSource = Okio.buffer(source(responseBody.source()))
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            private var totalBytesRead = 0L

            override fun read(sink: Buffer?, byteCount: Long): Long {
                if (sink == null) return totalBytesRead
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0L
                val contentLength = responseBody.contentLength() or Math.max(bytesRead, 5000L)
                val progress = if (bytesRead == -1L) 0L else (100 * bytesRead) / contentLength
                progressListener.invoke(progress.toInt())
                return bytesRead
            }
        }
    }
}