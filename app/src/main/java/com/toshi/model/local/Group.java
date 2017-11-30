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
import android.support.annotation.NonNull;

import com.toshi.util.FileUtil;
import com.toshi.view.BaseApplication;

import org.spongycastle.util.encoders.Hex;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentPointer;
import org.whispersystems.signalservice.api.messages.SignalServiceGroup;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

public class Group extends RealmObject {

    // This can be anything except 40/42, it needs to not collide with
    // the length of a public address as we distinguish groups from individuals
    // based on the length of IDs
    public static final int GROUP_ID_LENGTH = 32;

    @PrimaryKey
    private String id;
    private String title;
    private RealmList<User> members;
    private Avatar avatar;

    public Group(){}

    public Group(final List<User> members) {
        this.id = generateId();
        this.members = new RealmList<>();
        this.members.addAll(members);
    }

    public Single<Group> updateFromSignalGroup(final SignalServiceGroup group, final SignalServiceMessageReceiver messageReceiver) {
        this.id = Hex.toHexString(group.getGroupId());
        this.members = new RealmList<>();

        if (group.getName().isPresent()) {
            this.title = group.getName().get();
        }

        processAvatar(group, messageReceiver);

        if (group.getMembers().isPresent()) {
            return lookupUsers(group.getMembers().get())
                    .map(this.members::addAll)
                    .flatMapCompletable(__ -> processAvatar(group, messageReceiver))
                    .toSingleDefault(this);
        }

        return Single.just(this);
    }

    private Completable processAvatar(final SignalServiceGroup group, final SignalServiceMessageReceiver messageReceiver) {
        if (group.getAvatar().isPresent()) {

            return Single.fromCallable(() -> {
                final SignalServiceAttachmentPointer attachment = group.getAvatar().get().asPointer();
                return FileUtil.writeAttachmentToFileFromMessageReceiver(attachment, messageReceiver);
            })
            .flatMap(file -> FileUtil.compressImage(FileUtil.MAX_SIZE, file))
            .map(file -> BitmapFactory.decodeFile(file.getAbsolutePath()))
            .map(Avatar::new)
            .map(avatar -> this.avatar = avatar)
            .toCompletable();
        }

        return Completable.complete();
    }

    private Single<List<User>> lookupUsers(final List<String> userIds) {
        return Observable
                .from(userIds)
                .flatMap( uid -> BaseApplication
                            .get()
                            .getRecipientManager()
                            .getUserFromToshiId(uid)
                            .toObservable()
                )
                .toList()
                .toSingle()
                .subscribeOn(Schedulers.io());
    }

    public Group addMember(final User member) {
        if (!this.members.contains(member)) this.members.add(member);
        return this;
    }

    @NonNull
    public String getId() {
        return this.id;
    }

    @NonNull
    public byte[] getIdBytes() {
        return Hex.decode(this.id);
    }

    @NonNull
    public String getTitle() {
        return this.title == null ? "" : this.title;
    }

    public Group setTitle(final String title) {
        this.title = title;
        return this;
    }

    @NonNull
    public List<User> getMembers() {
        return this.members;
    }

    public Avatar getAvatar() {
        return this.avatar;
    }

    public Group setAvatar(final Bitmap avatar) {
        this.avatar = new Avatar(avatar);
        return this;
    }

    private String generateId() {
        try {
            byte[] groupId = new byte[GROUP_ID_LENGTH / 2];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(groupId);
            return Hex.toHexString(groupId);
        } catch (final NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    // Helper functions
    public List<String> getMemberIds() {
        if (this.members == null) {
            return Collections.emptyList();
        }
        final List<String> ids = new LinkedList<>();
        for (final User member : this.members) {
            ids.add(member.getToshiId());
        }
        return ids;
    }

    public List<SignalServiceAddress> getMemberAddresses() {
        if (this.members == null) {
            return Collections.emptyList();
        }
        final List<SignalServiceAddress> ids = new LinkedList<>();
        for (final User member : this.members) {
            ids.add(new SignalServiceAddress(member.getToshiId()));
        }
        return ids;
    }

    /* package */ boolean hasAvatar() {
        return this.avatar != null && this.avatar.getBytes() != null;
    }

    public static Single<Group> fromSignalGroup(final SignalServiceGroup signalGroup) {
        return fromId(Hex.toHexString(signalGroup.getGroupId()));
    }

    public static Single<Group> fromId(final String id) {
        return BaseApplication
                .get()
                .getRecipientManager()
                .getGroupFromId(id);
    }

    public static Group emptyGroup(final byte[] id) {
        final Group group = new Group();
        group.id = Hex.toHexString(id);
        return group;
    }
}
