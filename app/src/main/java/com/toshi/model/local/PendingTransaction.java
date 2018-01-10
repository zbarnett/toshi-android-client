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


import com.toshi.model.sofa.SofaMessage;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PendingTransaction extends RealmObject {

    @PrimaryKey
    private String txHash;
    private SofaMessage sofaMessage;

    public PendingTransaction() {}

    public String getTxHash() {
        return txHash;
    }

    public PendingTransaction setTxHash(final String txHash) {
        this.txHash = txHash;
        return this;
    }

    public SofaMessage getSofaMessage() {
        return sofaMessage;
    }

    public PendingTransaction setSofaMessage(final SofaMessage sofaMessage) {
        this.sofaMessage = sofaMessage;
        return this;
    }

    public void cascadeDelete() {
        if (this.sofaMessage != null) this.sofaMessage.cascadeDelete();
        deleteFromRealm();
    }
}
