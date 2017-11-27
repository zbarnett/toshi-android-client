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

package com.toshi.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.extensions.hideStatusBar
import com.toshi.util.ImageUtil
import kotlinx.android.synthetic.main.activity_fullscreen_image.image
import java.io.File

class FullscreenImageActivity : AppCompatActivity() {

    companion object {
        const val FILE_PATH = "filePath"
        const val IMAGE_URL = "imageUrl"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)
        init()
    }

    private fun init() {
        hideStatusBar()
        loadImage()
    }

    private fun loadImage() {
        val filePath = intent.getStringExtra(FILE_PATH)
        val imageUrl = intent.getStringExtra(IMAGE_URL)
        filePath?.let { ImageUtil.renderFileIntoTarget(File(it), image) }
        imageUrl?.let { ImageUtil.load(it, image) }
    }
}