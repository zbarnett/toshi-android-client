package com.toshi.util;

import android.support.annotation.IntDef;

public class BrowseType {
    @IntDef({VIEW_TYPE_TOP_RATED_APPS,
            VIEW_TYPE_LATEST_APPS,
            VIEW_TYPE_TOP_RATED_PUBLIC_USERS,
            VIEW_TYPE_LATEST_PUBLIC_USERS})
    public @interface Type {}
    public static final int VIEW_TYPE_TOP_RATED_APPS = 1;
    public static final int VIEW_TYPE_LATEST_APPS = 2;
    public static final int VIEW_TYPE_TOP_RATED_PUBLIC_USERS = 3;
    public static final int VIEW_TYPE_LATEST_PUBLIC_USERS = 4;
}
