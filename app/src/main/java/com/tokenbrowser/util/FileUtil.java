/*
 * 	Copyright (c) 2017. Token Browser, Inc
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

package com.tokenbrowser.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;

import com.tokenbrowser.BuildConfig;
import com.tokenbrowser.model.local.Attachment;
import com.tokenbrowser.view.BaseApplication;

import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentPointer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import rx.Single;
import rx.schedulers.Schedulers;

public class FileUtil {

    public static final int MAX_SIZE = 1024 * 1024;
    public static final String FILE_PROVIDER_NAME = ".fileProvider";

    public Single<File> saveFileFromUri(final Context context, final Uri uri) {
        return Single.fromCallable(() -> {
            final String mimeType = context.getContentResolver().getType(uri);
            final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            final String fileExtension = mimeTypeMap.getExtensionFromMimeType(mimeType);
            final String fileName = String.format("%s.%s", UUID.randomUUID().toString(), fileExtension);
            final File destFile = new File(BaseApplication.get().getFilesDir(), fileName);
            final InputStream inputStream = BaseApplication.get()
                    .getContentResolver()
                    .openInputStream(uri);
            return writeToFileFromInputStream(destFile, inputStream);
        })
        .subscribeOn(Schedulers.io());
    }

    private File writeToFileFromInputStream(final File file, final InputStream inputStream) throws IOException {
        final BufferedSink sink = Okio.buffer(Okio.sink(file));
        final Source source = Okio.source(inputStream);
        sink.writeAll(source);
        sink.close();
        return file;
    }

    public @Nullable File writeAttachmentToFileFromMessageReceiver(
            final SignalServiceAttachmentPointer attachment,
            final SignalServiceMessageReceiver messageReceiver) {
        File file = null;
        try {
            final String tempName = String.format("%d", attachment.getId());
            file = new File(BaseApplication.get().getCacheDir(), tempName);
            final InputStream inputStream = messageReceiver.retrieveAttachment(attachment, file);

            final File destFile = constructAttachmentFile(attachment.getContentType());
            return writeToFileFromInputStream(destFile, inputStream);
        } catch (IOException | InvalidMessageException e) {
            LogUtil.exception(getClass(), "Error during writing attachment to file", e);
            return null;
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    private File constructAttachmentFile(final String contentType) throws IOException {
        final File baseDirectory = BaseApplication.get().getFilesDir();
        final String directoryPath = contentType.startsWith("image/") ? "images" : "files";
        final File outputDirectory = new File(baseDirectory, directoryPath);

        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        final String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType);
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        final String baseName = dateFormatter.format(new Date());
        final String filename = String.format("%s.%s", baseName, extension);
        return new File(outputDirectory, filename);
    }

    public File createImageFileWithRandomName() {
        final String filename = UUID.randomUUID().toString() + ".jpg";
        return new File(BaseApplication.get().getFilesDir(), filename);
    }

    public String getMimeTypeFromFilename(final String filename) {
        if (filename == null) return null;
        final String strippedFilename = filename.replaceAll("\\s","");
        final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(strippedFilename);
        return  MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
    }

    public Single<File> compressImage(final long maxSize, final File file) {
        return Single.fromCallable(() -> {
            if (file.length() <= maxSize) return file;
            final int compressPercentage = (int)(((double)maxSize / file.length()) * 100);
            final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            final OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressPercentage, outputStream);
            return file;
        })
        .subscribeOn(Schedulers.io());
    }

    public Attachment getNameAndSizeFromUri(final Uri uri) {
        final String [] projection = { MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE };
        final Cursor cursor =
                BaseApplication.get()
                .getContentResolver()
                .query(
                        uri,
                        projection,
                        null,
                        null,
                        null
                );

        if (cursor == null) return null;
        final int columnIndexDisplayName = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
        final int columnIndexSize = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
        cursor.moveToFirst();
        final String displayName = cursor.getString(columnIndexDisplayName);
        final long size = cursor.getLong(columnIndexSize);
        cursor.close();

        return new Attachment()
                .setFilename(displayName)
                .setSize(size);
    }

    public String getFilenameFromPath(final String path) {
        final File file = new File(path);
        return file.exists() ? file.getName() : "";
    }

    public long getFileSize(final String path) {
        final File file = new File(path);
        return file.exists() ? file.length() : 0;
    }

    public Uri getUriFromFile(final File file) {
        return FileProvider
                .getUriForFile(
                        BaseApplication.get(),
                        BuildConfig.APPLICATION_ID + FILE_PROVIDER_NAME,
                        file
                );
    }
}
