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


import com.toshi.model.local.User;
import com.toshi.view.BaseApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import rx.Completable;
import rx.Single;
import rx.schedulers.Schedulers;

public class UserStore {

    private final static ExecutorService dbThread = Executors.newSingleThreadExecutor();

    public Single<User> loadForToshiId(final String toshiId) {
        return loadWhere("owner_address", toshiId);
    }

    public Single<User> loadForPaymentAddress(final String address) {
        return loadWhere("payment_address", address);
    }

    public Single<User> loadForUsername(final String username) {
        return loadWhere("username", username);

    }

    public Completable save(final User user) {
        return Completable.fromAction(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            realm.beginTransaction();
            realm.insertOrUpdate(user);
            realm.commitTransaction();
            realm.close();
        })
        .subscribeOn(Schedulers.from(dbThread));
    }

    private Single<User> loadWhere(final String fieldName, final String value) {
        return Single.fromCallable(() -> {
            final Realm realm = BaseApplication.get().getRealm();
            final User user = realm
                    .where(User.class)
                    .equalTo(fieldName, value)
                    .findFirst();

            final User queriedUser = user == null ? null : realm.copyFromRealm(user);
            realm.close();
            return queriedUser;
        })
        .subscribeOn(Schedulers.from(dbThread));
    }
}
