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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class PermissionUtil {

    public static final int CAMERA_PERMISSION = 1;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION = 2;

    public interface HasPermissionListener {
        void onHasPermission();
    }

    public static void requestPermission(@NonNull final AppCompatActivity activity,
                                         @NonNull final String permission,
                                         final int requestCode) {
        ActivityCompat
                .requestPermissions(
                        activity,
                        new String[] {permission},
                        requestCode
                );
    }

    public static void grantUriPermission(@NonNull final AppCompatActivity activity,
                                          @NonNull final Intent intent,
                                          @NonNull final Uri uri) {
        if (Build.VERSION.SDK_INT >= 21) return;
        final PackageManager pm = activity.getPackageManager();
        final String packageName = intent.resolveActivity(pm).getPackageName();
        activity
                .grantUriPermission(
                        packageName,
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    }

    public static void hasPermission(@NonNull final AppCompatActivity activity,
                                     @NonNull final String permission,
                                     final int requestCode,
                                     @NonNull final HasPermissionListener listener) {
        if (activity == null) return;
        final boolean hasPermission = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
        if (hasPermission) {
            listener.onHasPermission();
        } else {
            requestPermission(
                    activity,
                    permission,
                    requestCode
            );
        }
    }

    public static boolean isPermissionGranted(final int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}
