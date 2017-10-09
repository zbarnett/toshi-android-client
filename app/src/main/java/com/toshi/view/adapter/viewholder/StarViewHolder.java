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

package com.toshi.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.toshi.R;
import com.toshi.view.adapter.listeners.OnItemClickListener;

public class StarViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;

    public StarViewHolder(View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.star);
    }

    public void setWholeStar() {
        imageView.setImageResource(R.drawable.star);
    }

    public void setHalfStar() {
        imageView.setImageResource(R.drawable.star_half);
    }

    public void setWholeGreyStar() {
        imageView.setImageResource(R.drawable.star_grey);
    }

    public void setOnItemClickListener(final OnItemClickListener<Void> listener) {
        this.itemView.setOnClickListener(__ -> listener.onItemClick(null));
    }
}
