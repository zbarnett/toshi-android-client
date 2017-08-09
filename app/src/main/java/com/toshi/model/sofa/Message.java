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


import android.support.annotation.StringRes;

import com.toshi.R;
import com.toshi.view.BaseApplication;

import java.util.List;

public class Message {

    private String body;
    private List<Control> controls;
    // Default behaviour is to how the keyboard
    private boolean showKeyboard = true;

    public String getBody() {
        return this.body;
    }

    public Message setBody(final String body) {
        this.body = body;
        return this;
    }

    public List<Control> getControls() {
        return this.controls;
    }

    public boolean shouldHideKeyboard() {
        return !this.showKeyboard;
    }

    public String toUserVisibleString(final boolean sentByLocal, final boolean hasAttachment) {
        if (hasAttachment) return getAttachmentMessage(sentByLocal);
        else return this.body;
    }

    private String getAttachmentMessage(final boolean sentByLocal) {
        final @StringRes int successMessageId = sentByLocal
                ? R.string.latest_sent_attachment
                : R.string.latest_received_attachment;

        return BaseApplication.get().getResources().getString(successMessageId);
    }
}
