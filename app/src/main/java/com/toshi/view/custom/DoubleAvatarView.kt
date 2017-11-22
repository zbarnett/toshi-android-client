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
import android.widget.FrameLayout
import com.toshi.R
import com.toshi.extensions.getColorById
import com.toshi.extensions.isVisible
import com.toshi.util.ImageUtil
import kotlinx.android.synthetic.main.view_double_avatar.view.*

class DoubleAvatarView : FrameLayout {
    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int): super(context, attrs, defStyle) {
        init()
    }

    private fun init() = inflate(context, R.layout.view_double_avatar, this)

    fun loadAvatars(avatars: List<String>) {
        when (avatars.size) {
            0 -> showPlaceHolder()
            1 -> loadSingleAvatar(avatars[0])
            2 -> loadDoubleAvatars(avatars)
            else -> loadDoubleAvatars(avatars.takeLast(2))
        }
    }

    private fun showPlaceHolder() = singleAvatar.setBackgroundColor(getColorById(R.color.placeholder))

    private fun loadSingleAvatar(avatar: String) {
        hideDoubleAvatars()
        singleAvatar.isVisible(true)
        ImageUtil.load(avatar, singleAvatar)
    }

    private fun hideDoubleAvatars() {
        firstAvatar.isVisible(false)
        secondAvatar.isVisible(false)
    }

    private fun loadDoubleAvatars(avatars: List<String>) {
        singleAvatar.isVisible(false)
        firstAvatar.isVisible(true)
        secondAvatar.isVisible(true)
        ImageUtil.load(avatars[0], firstAvatar)
        ImageUtil.load(avatars[1], secondAvatar)
    }
}