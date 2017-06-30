/*
 * 	Copyright (c) 2017. Toshi Browser, Inc
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
import com.toshi.util.LogUtil;

import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentPointer;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentStream;

import java.io.FileNotFoundException;

public class Avatar {

    private long id;
    private byte[] key;
    private String contentType;
    private byte[] digest;
    private SignalServiceAttachmentStream attachmentStream;

    public Avatar(final Bitmap avatar) {
        try {
            init(avatar);
        } catch (final NullPointerException | FileNotFoundException | IllegalStateException ex) {
            LogUtil.e(getClass(), "Avatar not initialised. " + ex);
        }
    }

    private void init(final Bitmap avatar) throws FileNotFoundException, IllegalStateException {
        this.attachmentStream = FileUtil.buildSignalServiceAttachment(avatar);
        if (!this.attachmentStream.isPointer()) return;
        final SignalServiceAttachmentPointer pointer = this.attachmentStream.asPointer();
        init(pointer);
    }

    private void init(final SignalServiceAttachmentPointer pointer) {
        this.id = pointer.getId();
        this.key = pointer.getKey();
        this.contentType = pointer.getContentType();
        this.digest = pointer.getDigest().orNull();
    }

    public long getId() {
        return id;
    }

    public byte[] getKey() {
        return key;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getDigest() {
        return digest;
    }

    public SignalServiceAttachment getStream() {
        return this.attachmentStream;
    }
}
