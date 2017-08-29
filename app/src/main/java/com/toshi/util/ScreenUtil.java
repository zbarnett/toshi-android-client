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

package com.toshi.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.toshi.view.BaseApplication;

public class ScreenUtil {

    public static int getWidthOfScreen() {
        final Point size = new Point();
        getDisplay().getSize(size);
        return size.x;
    }

    public static double getScreenRatio() {
        final Point size = new Point();
        getDisplay().getSize(size);
        if (isPortrait()) {
            return (double)size.y / (double)size.x;
        }
        return (double)size.x / (double)size.y;
    }

    private static Display getDisplay() {
        final WindowManager wm = (WindowManager) BaseApplication.get().getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay();
    }

    public static boolean isPortrait() {
        return BaseApplication
                .get()
                .getResources()
                .getConfiguration()
                .orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
