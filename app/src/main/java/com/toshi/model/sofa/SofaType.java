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

package com.toshi.model.sofa;


import android.support.annotation.IntDef;

public class SofaType {

    @IntDef({
            UNKNOWN,
            PLAIN_TEXT,
            PAYMENT_REQUEST,
            COMMAND_REQUEST,
            PAYMENT,
            INIT,
            INIT_REQUEST,
            TIMESTAMP,
            LOCAL_STATUS_MESSAGE
    })
    public @interface Type {}
    public static final int UNKNOWN = -1;
    public static final int PLAIN_TEXT = 0;
    public static final int PAYMENT_REQUEST = 1;
    public static final int COMMAND_REQUEST = 2;
    public static final int PAYMENT = 3;
    public static final int INIT = 4;
    public static final int INIT_REQUEST = 5;
    public static final int IMAGE = 6;
    public static final int TIMESTAMP = 7;
    public static final int FILE = 8;
    public static final int LOCAL_STATUS_MESSAGE = 9;

    public static final String LOCAL_ONLY_PAYLOAD = "custom_local_only_payload";
    public static final String WEB_VIEW = "webview:";
    public static final String CONFIRMED = "confirmed";
    public static final String UNCONFIRMED = "unconfirmed";
    public static final String FAILED = "failed";

    /* package */ static final String PAYMENT_ADDRESS = "paymentAddress";
    /* package */ static final String LANGUAGE = "language";

    private static final String plain_text = "SOFA::Message:";
    private static final String command_request = "SOFA::Command:";
    private static final String payment_request = "SOFA::PaymentRequest:";
    private static final String payment = "SOFA::Payment:";
    private static final String init = "SOFA::Init:";
    private static final String init_request = "SOFA::InitRequest:";
    private static final String timestamp = "LOCAL::Timestamp";
    private static final String local_status_message = "LOCAL::StatusMessage";

    /* package */ static String createHeader(final @SofaType.Type int type) {
        switch (type) {
            case PLAIN_TEXT: return plain_text;
            case PAYMENT_REQUEST: return payment_request;
            case COMMAND_REQUEST: return command_request;
            case PAYMENT: return payment;
            case INIT: return init;
            case INIT_REQUEST: return init_request;
            case TIMESTAMP: return timestamp;
            case LOCAL_STATUS_MESSAGE: return local_status_message;
            case UNKNOWN: return null;
            default: return null;
        }
    }

    public static @Type int getType(final String header) {
        if (header == null) return UNKNOWN;
        switch (header) {
            case plain_text: return PLAIN_TEXT;
            case payment_request: return PAYMENT_REQUEST;
            case command_request: return COMMAND_REQUEST;
            case payment: return PAYMENT;
            case init: return INIT;
            case init_request: return INIT_REQUEST;
            case timestamp: return TIMESTAMP;
            case local_status_message: return LOCAL_STATUS_MESSAGE;
            default: return UNKNOWN;
        }
    }
}
