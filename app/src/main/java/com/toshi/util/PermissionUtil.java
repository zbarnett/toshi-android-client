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
}
