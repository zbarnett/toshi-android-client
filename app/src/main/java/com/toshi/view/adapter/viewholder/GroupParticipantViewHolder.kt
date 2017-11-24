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

package com.toshi.view.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.toshi.R
import com.toshi.model.local.User
import com.toshi.util.ImageUtil

class GroupParticipantViewHolder(
        view: View,
        attachContextMenu: Boolean = false
        ) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {

    companion object {
        val MENU_VIEW_PROFILE = 0
        val MENU_MESSAGE = 1
    }

    private val avatar: ImageView = view.findViewById<View>(R.id.avatar) as ImageView
    private val name: TextView = view.findViewById<View>(R.id.name) as TextView
    private val username: TextView = view.findViewById<View>(R.id.username) as TextView

    init {
        if (attachContextMenu) { view.setOnCreateContextMenuListener(this) }
    }

    fun setUser(user: User): GroupParticipantViewHolder {
        this.name.text = user.displayName
        this.username.text = user.username
        ImageUtil.load(user.avatar, this.avatar)
        return this
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.add(0, MENU_VIEW_PROFILE, adapterPosition, v?.resources?.getString(R.string.view_profile))
        menu?.add(0, MENU_MESSAGE, adapterPosition, v?.resources?.getString(R.string.message))
    }
}
