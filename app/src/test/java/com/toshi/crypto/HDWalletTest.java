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

package com.toshi.crypto;


import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HDWalletTest {

    private final String expectedMasterSeed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
    // Path `m/0'/1/0/0
    private final String expectedOwnerAddress = "0xa391af6a522436f335b7c6486640153641847ea2";

    // Path `m/44'/60'/0'/0'
    private final String expectedPaymentAddress = "0x9858effd232b4033e47d90003d41ec34ecaeda94";

    // Mocks
    private SharedPreferences sharedPreferencesMock;

    @Before
    public void setup() {
        this.sharedPreferencesMock = Mockito.mock(SharedPreferences.class);
        Mockito
                .when(this.sharedPreferencesMock.getString(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(this.expectedMasterSeed);
    }

    @Test
    public void walletCreatedFromSeedUsesThatSeed() {
        final HDWallet wallet =
                new HDWallet(this.sharedPreferencesMock)
                .getExistingWallet()
                .toBlocking()
                .value();
        assertThat(wallet.getMasterSeed(), is(this.expectedMasterSeed));
    }

    @Test
    public void walletCreatedFromSeedDerivesCorrectOwnerAddress() {
        final HDWallet wallet =
                new HDWallet(this.sharedPreferencesMock)
                        .getExistingWallet()
                        .toBlocking()
                        .value();
        assertThat(wallet.getOwnerAddress(), is(this.expectedOwnerAddress));
    }

    @Test
    public void walletCreatedFromSeedDerivesCorrectPaymentAddress() {
        final HDWallet wallet =
                new HDWallet(this.sharedPreferencesMock)
                        .getExistingWallet()
                        .toBlocking()
                        .value();
        assertThat(wallet.getPaymentAddress(), is(this.expectedPaymentAddress));
    }
}
