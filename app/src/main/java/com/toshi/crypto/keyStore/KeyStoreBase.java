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
package com.toshi.crypto.keyStore;

import android.content.Context;

import com.toshi.exception.KeyStoreException;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public abstract class KeyStoreBase {

    public interface KeystoreListener {
        // Called when the security has been updated and the new encrypted data needs storing
        void onUpdate(final String encryptedData);
    }

    /*package */ static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    /*package */ static final String UTF_8 = "UTF-8";
    /*package */ String alias;
    /*package */ KeyStore keyStore;
    /*package */ Context context;

    /*package */ KeyStoreBase() {}

    /*package */ KeyStoreBase(final Context context, final String alias) throws KeyStoreException {
        this.alias = alias;
        initKeyStore(context);
    }

    private void initKeyStore(final Context context) throws KeyStoreException {
        this.context = context;
        try {
            this.keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            this.keyStore.load(null);
            createNewKeysIfNeeded();
        } catch (IOException | NoSuchAlgorithmException | CertificateException | java.security.KeyStoreException e) {
            throw new KeyStoreException(new Throwable(e.getMessage()));
        }
    }

    abstract /*package */ void createNewKeysIfNeeded() throws KeyStoreException;

    abstract public String encrypt(final String stringToEncrypt) throws KeyStoreException;

    abstract public String decrypt(final String encryptedData, final KeystoreListener updateListener) throws KeyStoreException;

    public void deleteKey() throws KeyStoreException {
        try {
            this.keyStore.deleteEntry(this.alias);
        } catch (java.security.KeyStoreException e) {
            throw new KeyStoreException(new Throwable(e.getMessage()));
        }
    }
}
