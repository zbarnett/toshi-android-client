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
import android.view.View
import android.widget.TextView
import com.toshi.R
import com.toshi.extensions.isVisible
import com.toshi.util.LocaleUtil
import com.toshi.view.BaseApplication
import java.text.SimpleDateFormat
import java.util.Calendar

class TimestampMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val dayView: TextView = itemView.findViewById(R.id.day)
    private val timeView: TextView = itemView.findViewById(R.id.time)

    fun setTime(timestamp: Long) {
        if (isOlderThanOneWeek(timestamp)) {
            timeView.text = getTimeFromTimestamp(timestamp, "MMM d")
            dayView.isVisible(false)
        } else {
            dayView.text = getDayFromTimestamp(timestamp, "E")
            timeView.text = getTimeFromTimestamp(timestamp, "H:mma")
            dayView.isVisible(true)
        }
    }

    private fun isOlderThanOneWeek(timestamp: Long): Boolean {
        val past = Calendar.getInstance(LocaleUtil.getLocale())
        past.timeInMillis = timestamp
        val week = Calendar.getInstance(LocaleUtil.getLocale())
        week.add(Calendar.DAY_OF_YEAR, -7)
        return week.get(Calendar.DAY_OF_YEAR) > past.get(Calendar.DAY_OF_YEAR)
    }

    private fun getDayFromTimestamp(timestamp: Long, pattern: String): String {
        return when {
            isToday(timestamp) -> BaseApplication.get().getString(R.string.today)
            wasYesterday(timestamp) -> BaseApplication.get().getString(R.string.yesterday)
            else -> getTimeFromTimestamp(timestamp, pattern)
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val past = Calendar.getInstance(LocaleUtil.getLocale())
        past.timeInMillis = timestamp
        val now = Calendar.getInstance(LocaleUtil.getLocale())
        return now.get(Calendar.DAY_OF_YEAR) == past.get(Calendar.DAY_OF_YEAR)
    }

    private fun wasYesterday(timestamp: Long): Boolean {
        val past = Calendar.getInstance(LocaleUtil.getLocale())
        past.timeInMillis = timestamp
        val yesterday = Calendar.getInstance(LocaleUtil.getLocale())
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return yesterday.get(Calendar.DAY_OF_YEAR) == past.get(Calendar.DAY_OF_YEAR)
    }

    private fun getTimeFromTimestamp(timestamp: Long, pattern: String): String {
        val past = Calendar.getInstance(LocaleUtil.getLocale())
        past.timeInMillis = timestamp
        val sdf = SimpleDateFormat(pattern, LocaleUtil.getLocale())
        return sdf.format(past.time)
    }
}
