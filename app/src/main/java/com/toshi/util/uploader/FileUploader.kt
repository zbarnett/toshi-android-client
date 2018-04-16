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

package com.toshi.util.uploader

import com.toshi.manager.network.IdInterface
import com.toshi.model.local.User
import com.toshi.util.FileUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import rx.Single
import java.io.File

class FileUploader(
        private val idService: IdInterface
) {

    companion object {
        private const val FORM_DATA_NAME = "Profile-Image-Upload"
    }

    fun uploadAvatar(file: File): Single<User> {
        val mimeType = FileUtil.getMimeTypeFromFilename(file.name) ?: return Single.error(IllegalArgumentException("Unable to determine file type from file."))
        val mediaType = MediaType.parse(mimeType)
        val requestFile = RequestBody.create(mediaType, file)
        val body = MultipartBody.Part.createFormData(FORM_DATA_NAME, file.name, requestFile)

        return idService.timestamp
                .flatMap { idService.uploadFile(body, it.get()) }
    }
}