package com.toshi.manager.store;

import com.toshi.model.local.BlockedUser;
import com.toshi.view.BaseApplication;

import io.realm.Realm;
import rx.Single;

public class BlockedUserStore {

    public Single<Boolean> isBlocked(final String address) {
        return Single.fromCallable(() -> loadWhere("owner_address", address))
                .map(blockedUser -> blockedUser != null);
    }

    private BlockedUser loadWhere(final String fieldName, final String value) {
        final Realm realm = BaseApplication.get().getRealm();
        final BlockedUser user =
                realm.where(BlockedUser.class)
                        .equalTo(fieldName, value)
                        .findFirst();
        final BlockedUser queriedBlockedUser = user == null ? null : realm.copyFromRealm(user);
        realm.close();
        return queriedBlockedUser;
    }

    public void save(final BlockedUser blockedUser) {
        final Realm realm = BaseApplication.get().getRealm();
        realm.beginTransaction();
        realm.insertOrUpdate(blockedUser);
        realm.commitTransaction();
        realm.close();
    }

    public void delete(final String ownerAddress) {
        final Realm realm = BaseApplication.get().getRealm();
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
