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

package com.tokenbrowser.model.local;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Recipient extends RealmObject {

    @PrimaryKey
    private String id;
    private User user;
    private Group group;

    public Recipient() {}

    public Recipient(final User user) {
        this.id = UUID.randomUUID().toString();
        this.user = user;
    }

    public Recipient(final Group group) {
        this.id = UUID.randomUUID().toString();
        this.group = group;
    }

    public boolean isGroup() {
        return this.group != null;
    }

    public User getUser() {
        return this.user;
    }

    public Group getGroup() {
        return this.group;
    }
}
