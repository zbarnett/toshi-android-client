package com.toshi.util;

import android.support.annotation.IntDef;

public class ScannerResultType {
    @IntDef({
            NO_ACTION,
            PAYMENT_ADDRESS
    })
    public @interface TYPE {}
    public static final int NO_ACTION = 0;
    public static final int PAYMENT_ADDRESS = 1;
}
