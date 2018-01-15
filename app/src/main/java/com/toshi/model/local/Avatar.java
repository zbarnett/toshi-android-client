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
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.toshi.util.FileUtil;
import com.toshi.util.ImageUtil;
import com.toshi.util.LogUtil;

import org.spongycastle.util.encoders.Hex;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentPointer;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentStream;
import org.whispersystems.signalservice.api.messages.SignalServiceGroup;

import java.io.File;
import java.io.FileNotFoundException;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import rx.Single;

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

    public static Single<Avatar> processFromSignalGroup(final SignalServiceGroup group, final SignalServiceMessageReceiver messageReceiver) {
        if (group.getAvatar().isPresent()) {
            return Single.fromCallable(() -> {
                final SignalServiceAttachmentPointer attachment = group.getAvatar().get().asPointer();
                final String groupId = Hex.toHexString(group.getGroupId());
                return FileUtil.writeAvatarToFileFromMessageReceiver(attachment, messageReceiver, groupId);
            })
            .flatMap(Avatar::compressImage)
            .map(File::getAbsolutePath)
            .map(BitmapFactory::decodeFile)
            .map(Avatar::new);
        }

        return Single.just(new Avatar());
    }

    private static Single<File> compressImage(@Nullable final File file) {
        if (file == null) return Single.error(new Throwable("File is null when trying to compress it"));
        return FileUtil.compressImage(FileUtil.MAX_SIZE, file);
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
