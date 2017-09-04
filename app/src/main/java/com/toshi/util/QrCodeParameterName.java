package com.toshi.util;

import android.support.annotation.StringDef;

public class QrCodeParameterName {
    @StringDef({
            VALUE,
            AMOUNT
    })
    public @interface TYPE {}
    // "value" is used by Toshi and others
    public static final String VALUE = "value";
    //"amount" is used by Jaxx and others
    public static final String AMOUNT = "amount";
    public static final String MEMO = "memo";
}
