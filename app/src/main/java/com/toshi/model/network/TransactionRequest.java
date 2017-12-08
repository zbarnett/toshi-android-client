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

package com.toshi.model.network;


public class TransactionRequest {

    private String value;
    private String from;
    private String to;
    private String data;
    private String gas;
    private String gasPrice;
    private String nonce;
    private String chainId;

    public TransactionRequest setValue(final String value) {
        this.value = value;
        return this;
    }

    public TransactionRequest setToAddress(final String addressInHex) {
        this.to = addressInHex;
        return this;
    }

    public TransactionRequest setFromAddress(final String addressInHex) {
        this.from = addressInHex;
        return this;
    }

    public TransactionRequest setData(final String data) {
        this.data = data;
        return this;
    }

    public TransactionRequest setGas(final String gas) {
        this.gas = gas;
        return this;
    }

    public TransactionRequest setGasPrice(final String gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    public TransactionRequest setNonce(final String nonce) {
        this.nonce = nonce;
        return this;
    }

    public TransactionRequest setChainId(final String chainId) {
        this.chainId = chainId;
        return this;
    }
}
