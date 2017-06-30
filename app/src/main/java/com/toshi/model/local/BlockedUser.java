package com.toshi.model.local;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BlockedUser extends RealmObject {
    @PrimaryKey
    private String owner_address;

    public BlockedUser setOwnerAddress(final String ownerAddress) {
        this.owner_address = ownerAddress;
        return this;
    }
}
