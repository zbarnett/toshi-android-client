package com.toshi.model.network;

import android.content.Context;
import android.support.annotation.StringDef;

import com.toshi.R;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class SofaError extends RealmObject {

    @StringDef({
            INSUFFUCIENT_FUNDS,
            NOT_DELIVERED_ID,
            INVALID_SIGNATURE
    })
    public @interface TYPE {}
    @Ignore private static final String INSUFFUCIENT_FUNDS = "insufficient_funds";
    @Ignore private static final String INVALID_SIGNATURE = "invalid_signature";
    @Ignore private static final String NOT_DELIVERED_ID = "not_delivered";
    @Ignore private static final String USER_UNAVAILABLE = "user_unavailable";

    @PrimaryKey
    private String id;
    private String message;

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public SofaError createNotDeliveredMessage(final Context context) {
        this.id = NOT_DELIVERED_ID;
        this.message = context.getString(R.string.not_delivered);
        return this;
    }

    public SofaError createUserUnavailableMessage(final Context context) {
        this.id = USER_UNAVAILABLE;
        this.message = context.getString(R.string.user_unavailable);
        return this;
    }

    public String getUserReadableErrorMessage(final Context context) {
        switch (this.id) {
            case INSUFFUCIENT_FUNDS: {
                return context.getString(R.string.insufficient_funds);
            }
            case USER_UNAVAILABLE: {
                return context.getString(R.string.user_unavailable);
            }
            default: {
                return context.getString(R.string.not_delivered);
            }
        }
    }
}
