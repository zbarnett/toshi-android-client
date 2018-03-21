package com.toshi.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.toshi.view.BaseApplication;

public class GcmPrefsUtil {

    public static void setEthGcmTokenSentToServer(final String networkId, final boolean isSentToServer) {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(networkId, isSentToServer).commit();
    }

    public static boolean isEthGcmTokenSentToServer(final String networkId) {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(networkId, false);
    }

    public static void clear() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
