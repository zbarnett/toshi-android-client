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
