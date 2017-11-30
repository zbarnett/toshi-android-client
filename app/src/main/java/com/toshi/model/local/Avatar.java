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

package com.toshi.model.local;


import android.graphics.Bitmap;

import com.toshi.util.FileUtil;
import com.toshi.util.ImageUtil;
import com.toshi.util.LogUtil;

import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentStream;

import java.io.FileNotFoundException;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Avatar extends RealmObject {
    private byte[] bytes;

    @Ignore
    private SignalServiceAttachmentStream attachmentStream;

    public Avatar() {}

    /* package */ Avatar(final Bitmap avatar) {
        try {
            init(avatar);
        } catch (final NullPointerException | FileNotFoundException | IllegalStateException ex) {
            LogUtil.e(getClass(), "Avatar not initialised. " + ex);
        }
    }

    private void init(final Bitmap avatar) throws FileNotFoundException, IllegalStateException {
        if (avatar == null) throw new NullPointerException("avatar is null");
        this.bytes = ImageUtil.toByteArray(avatar);
        generateAttachmentStream();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public SignalServiceAttachment getStream() {
        if (this.attachmentStream == null) generateAttachmentStream();
        return this.attachmentStream;
    }

    private void generateAttachmentStream() {
        if (this.bytes == null) return;
        this.attachmentStream = FileUtil.buildSignalServiceAttachment(this.bytes);
    }
}
