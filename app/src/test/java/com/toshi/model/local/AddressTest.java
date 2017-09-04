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


import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AddressTest {

    final String expectedEthAddress = "0xc5496aee77c1ba1f0854206a26dda82a81d6d8";

    @Test
    public void initWithAddressReturnsCorrectAddress() {
        final Address address = new Address(expectedEthAddress);
        assertThat(address.getHexAddress(), is(expectedEthAddress));
    }

    @Test
    public void initWithoutHexPrefixReturnsCorrectAddress() {
        final String ethAddress = expectedEthAddress.replace("0x", "");
        final Address address = new Address(ethAddress);
        assertThat(address.getHexAddress(), is(expectedEthAddress));
    }

    @Test
    public void initWithEthereumPrefixReturnsCorrectAddress() {
        final String ethAddress = "ethereum:" + expectedEthAddress;
        final Address address = new Address(ethAddress);
        assertThat(address.getHexAddress(), is(expectedEthAddress));
    }

    @Test
    public void initWithEthereumPrefixAndMissingHexPrefixReturnsCorrectAddress() {
        final String ethAddress = "ethereum:" + expectedEthAddress.replace("0x", "");
        final Address address = new Address(ethAddress);
        assertThat(address.getHexAddress(), is(expectedEthAddress));
    }

    @Test
    public void initWithIbanPrefixReturnsCorrectAddress() {
        final String icanAddress = "iban:XE7338O073KYGTWWZN0F2WZ0R8PX5ZPPZS";
        final Address address = new Address(icanAddress);
        assertThat(address.getHexAddress(), is(expectedEthAddress));
    }

    @Test
    public void initWithAddressAndAmountReturnsCorrectAddress() {
        final String ethAddress = expectedEthAddress + "?amount=5";
        final Address address = new Address(ethAddress);
        assertThat(address.getHexAddress(), is(expectedEthAddress));
    }

    @Test
    public void initWithAmountReturnsCorrectAmount() {
        final String ethAddress = expectedEthAddress + "?amount=5";
        final Address address = new Address(ethAddress);
        assertThat(address.getAmount(), is("5"));
    }

    @Test
    public void initWithoutAmountReturnsEmptyString() {
        final Address address = new Address(expectedEthAddress);
        assertThat(address.getAmount(), is(""));
    }

    @Test
    public void initWithSeveralArgumentsReturnsCorrectAmount() {
        final String ethAddress = expectedEthAddress + "?unused=2&amount=5";
        final Address address = new Address(ethAddress);
        assertThat(address.getAmount(), is("5"));
    }

    @Test
    public void initWithMemoReturnsCorrectMemo() {
        final String ethAddress = expectedEthAddress + "?memo=Hello";
        final Address address = new Address(ethAddress);
        assertThat(address.getMemo(), is("Hello"));
    }

    @Test
    public void initWithoutMemoReturnsEmptyString() {
        final Address address = new Address(expectedEthAddress);
        assertThat(address.getMemo(), is(""));
    }

    @Test
    public void initWithSeveralArgumentsReturnsCorrectMemo() {
        final String ethAddress = expectedEthAddress + "?unused=2&memo=Hello";
        final Address address = new Address(ethAddress);
        assertThat(address.getMemo(), is("Hello"));
    }

    @Test
    public void initWithInvalidAddressSetsAddressToEmptyString() {
        final String ethAddress = "thisisnotanaddress";
        final Address address = new Address(ethAddress);
        assertThat(address.getHexAddress(), is(""));
    }

    @Test
    public void initWithValidAddressSetsAddressToValid() {
        final Address address = new Address(expectedEthAddress);
        assertTrue(address.isValid());
    }

    @Test
    public void initWithInvalidAddressSetsAddressToInvalid() {
        final String ethAddress = "thisisnotanaddress";
        final Address address = new Address(ethAddress);
        assertFalse(address.isValid());
    }
}
