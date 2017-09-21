package com.toshi.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.toshi.view.BaseApplication;

public class GcmPrefsUtil {

    private static final String CHAT_SERVICE_SENT_TOKEN_TO_SERVER = "chatServiceSentTokenToServer";

    public static void setEthGcmTokenSentToServer(final String networkId, final boolean isSentToServer) {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(networkId, isSentToServer).commit();
    }

    public static boolean isEthGcmTokenSentToServer(final String networkId) {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(networkId, false);
    }

    public static void setChatGcmTokenSentToServer(final boolean isSentToServer) {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(CHAT_SERVICE_SENT_TOKEN_TO_SERVER, isSentToServer).commit();
    }

    public static boolean isChatGcmTokenSentToServer() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false);
    }

    public static void clear() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
