package com.tokenbrowser.manager.store;

import com.tokenbrowser.model.local.BlockedUser;

import io.realm.Realm;
import rx.Single;

public class BlockedUserStore {

    public Single<Boolean> isBlocked(final String address) {
        return Single.fromCallable(() -> loadWhere("owner_address", address))
                .map(blockedUser -> blockedUser != null);
    }

    private BlockedUser loadWhere(final String fieldName, final String value) {
        final Realm realm = Realm.getDefaultInstance();
        final BlockedUser user =
                realm.where(BlockedUser.class)
                        .equalTo(fieldName, value)
                        .findFirst();
        final BlockedUser retVal = user == null ? null : realm.copyFromRealm(user);
        realm.close();
        return retVal;
    }

    public void save(final BlockedUser blockedUser) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(blockedUser);
        realm.commitTransaction();
        realm.close();
    }

    public void delete(final String ownerAddress) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm
                .where(BlockedUser.class)
                .equalTo("owner_address", ownerAddress)
                .findFirst()
                .deleteFromRealm();
        realm.commitTransaction();
        realm.close();
    }
}
