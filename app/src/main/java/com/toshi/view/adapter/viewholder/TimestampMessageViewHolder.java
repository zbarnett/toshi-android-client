/*
 * 	Copyright (c) 2017. Toshi Browser, Inc
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

package com.toshi.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.toshi.R;
import com.toshi.util.LocaleUtil;
import com.toshi.view.BaseApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimestampMessageViewHolder extends RecyclerView.ViewHolder {

    private TextView day;
    private TextView time;

    public TimestampMessageViewHolder(View itemView) {
        super(itemView);
        this.day = (TextView) itemView.findViewById(R.id.day);
        this.time = (TextView) itemView.findViewById(R.id.time);
    }

    public void setTime(final long timestamp) {
        final String day = getDayFromTimestamp(timestamp, "E");
        final String time = getTimeFromTimestamp(timestamp, "H:mma");
        this.day.setText(day);
        this.time.setText(time);
    }

    private String getDayFromTimestamp(final long timestamp, final String pattern) {
        if (isToday(timestamp)) {
            return BaseApplication.get().getString(R.string.today);
        } else if (wasYesterday(timestamp)) {
            return BaseApplication.get().getString(R.string.yesterday);
        } else {
            return getTimeFromTimestamp(timestamp, pattern);
        }
    }

    private boolean isToday(final long timestamp) {
        final Calendar past = Calendar.getInstance(LocaleUtil.getLocale());
        past.setTimeInMillis(timestamp);
        final Calendar now = Calendar.getInstance(LocaleUtil.getLocale());
        return now.get(Calendar.DAY_OF_YEAR) == past.get(Calendar.DAY_OF_YEAR);
    }

    private boolean wasYesterday(final long timestamp) {
        final Calendar past = Calendar.getInstance(LocaleUtil.getLocale());
        past.setTimeInMillis(timestamp);
        final Calendar yesterday = Calendar.getInstance(LocaleUtil.getLocale());
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        return yesterday.get(Calendar.DAY_OF_YEAR) == past.get(Calendar.DAY_OF_YEAR);
    }

    private String getTimeFromTimestamp(final long timestamp, final String pattern) {
        final Calendar past = Calendar.getInstance(LocaleUtil.getLocale());
        past.setTimeInMillis(timestamp);
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, LocaleUtil.getLocale());
        return sdf.format(past.getTime());
    }
}
