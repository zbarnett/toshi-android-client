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

import android.annotation.TargetApi;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.toshi.exception.KeyStoreException;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

@TargetApi(23)
public class KeystoreHandler23 extends KeyStoreBase {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private String encryptionIv = "fixed_enc_iv";

    /*package */ KeystoreHandler23(final Context context, final String alias) throws KeyStoreException {
        super(context, alias);
    }

    @Override
    protected void createNewKeysIfNeeded() throws KeyStoreException {
        try {
            if (this.keyStore.containsAlias(this.alias)) return;

            final KeyGenerator keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, KeyStoreBase.ANDROID_KEY_STORE);

            keyGenerator.init(new KeyGenParameterSpec.Builder(this.alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build());

            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException | java.security.KeyStoreException e) {
            throw new KeyStoreException(new Throwable(e.getMessage()));
        }
    }

    @Override
    public String encrypt(final String textToEncrypt) throws KeyStoreException {
        try {
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec);
            final byte[] encryptedData = cipher.doFinal(textToEncrypt.getBytes(UTF_8));
            return Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | BadPaddingException | InvalidKeyException
                | NoSuchPaddingException | IllegalBlockSizeException | UnsupportedEncodingException
                | InvalidAlgorithmParameterException | java.security.KeyStoreException
                | UnrecoverableEntryException e) {
            throw new KeyStoreException(new Throwable(e.getMessage()));
        }
    }

    @Override
    public String decrypt(final String encryptedData, final KeyStoreBase.KeystoreListener listener) throws KeyStoreException {
        final String oldVersion = tryDecryptOldKeystoreVersion(encryptedData);
        if (oldVersion != null) {
            return portOldKeystoreVersion(oldVersion, listener);
        }
        return decryptCurrentKeystoreVersion(encryptedData);
    }

    private String portOldKeystoreVersion(final String oldVersion, final KeystoreListener listener) throws KeyStoreException {
        final String encryptedData = encrypt(oldVersion);
        listener.onUpdate(encryptedData);
        return oldVersion;
    }

    private String tryDecryptOldKeystoreVersion(final String encryptedData) throws KeyStoreException {
        final String oldVersionDecryption = new KeyStoreHandler16().decrypt(encryptedData, null);
        // If the old version wasn't able to decrypt the data then return null.
        if (oldVersionDecryption.equals(encryptedData)) return null;
        return oldVersionDecryption;
    }

    @NonNull
    private String decryptCurrentKeystoreVersion(String encryptedData) throws KeyStoreException {
        try {
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);

            final byte[] encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
            final byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, UTF_8);
        } catch (UnrecoverableEntryException | UnsupportedEncodingException
                | IllegalBlockSizeException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | InvalidKeyException | java.security.KeyStoreException
                | BadPaddingException | NoSuchAlgorithmException e) {
            throw new KeyStoreException(new Throwable(e.getMessage()));
        }
    }

    private SecretKey getSecretKey() throws UnrecoverableEntryException, NoSuchAlgorithmException, java.security.KeyStoreException {
        return ((KeyStore.SecretKeyEntry) this.keyStore.getEntry(this.alias, null)).getSecretKey();
    }
}
