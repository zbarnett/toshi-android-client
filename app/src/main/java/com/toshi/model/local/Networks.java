package com.toshi.model.local;


import android.support.annotation.Nullable;

import com.toshi.R;
import com.toshi.util.SharedPrefsUtil;
import com.toshi.view.BaseApplication;

import java.util.ArrayList;
import java.util.List;

public class Networks {
    private static Networks instance;
    private List<Network> networks;

    public static Networks getInstance() {
        if (instance == null) {
            instance = new Networks();
        }
        return instance;
    }

    private Networks() {
        this.networks = loadNetworkList();
    }

    private List<Network> loadNetworkList() {
        final String[] networkArray = BaseApplication.get().getResources().getStringArray(R.array.networks);
        final List<Network> networks = new ArrayList<>();
        for (final String networkDescription : networkArray) {
            networks.add(new Network(networkDescription));
        }
        return networks;
    }

    public List<Network> getNetworks() {
        return this.networks;
    }

    public boolean isDefaultNetwork() {
        // If the current network id is unknown then revert to default.
        if (getCurrentNetworkId() == null) return true;

        return getCurrentNetworkId().equals(getDefaultNetwork().getId());
    }

    public Network getCurrentNetwork() {
        try {
            return getNetworkById(getCurrentNetworkId());
        } catch (final NullPointerException ex) {
            return getDefaultNetwork();
        }
    }

    private Network getNetworkById(final @Nullable String id) throws NullPointerException {
        for (final Network network : this.networks) {
            if (network.getId().equals(id)) return network;
        }
        throw new NullPointerException("No network exists with that ID");
    }

    private Network getDefaultNetwork() {
        return this.networks.get(0);
    }

    private @Nullable String getCurrentNetworkId() {
        return SharedPrefsUtil.getCurrentNetworkId();
    }
}