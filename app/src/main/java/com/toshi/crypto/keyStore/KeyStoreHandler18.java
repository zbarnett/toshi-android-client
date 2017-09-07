package com.toshi.crypto.keyStore;

import android.annotation.TargetApi;
import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import com.toshi.exception.KeyStoreException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

@TargetApi(18)
public class KeyStoreHandler18 extends KeyStoreBase {

    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final String RSA = "RSA";
    private static final String APP_INFO = "CN=Toshi, O=Toshi Inc";

    /*package */ KeyStoreHandler18(final Context context, final String alias) throws KeyStoreException {
        super(context, alias);
    }

    @Override
    protected void createNewKeysIfNeeded() throws KeyStoreException {
        try {
            if (this.keyStore.containsAlias(this.alias)) return;

            final Calendar start = Calendar.getInstance();
            final Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);

            final KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(this.alias)
                    .setSubject(new X500Principal(APP_INFO))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(start.getTime())
                    .build();

            final KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(RSA, ANDROID_KEY_STORE);
            kpGenerator.initialize(spec);
            kpGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public String encrypt(final String stringToEncrypt) throws KeyStoreException {
        try {
            final Cipher input = Cipher.getInstance(RSA_MODE);
            input.init(Cipher.ENCRYPT_MODE, getPublicKey());

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);
            cipherOutputStream.write(stringToEncrypt.getBytes(UTF_8));
            cipherOutputStream.close();

            final byte[] encryptedData = outputStream.toByteArray();
            return Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (Exception e) {
            throw new KeyStoreException(new Throwable(e.getMessage()));
        }
    }

    @Override
    public String decrypt(final String encryptedData) throws KeyStoreException {
        try {
            final Cipher output = Cipher.getInstance(RSA_MODE);
            output.init(Cipher.DECRYPT_MODE, getPrivateKey());

            final byte[] encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
            final CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(encryptedBytes), output);
            final ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }
            cipherInputStream.close();

            final byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) bytes[i] = values.get(i);

            return new String(bytes, 0, bytes.length, UTF_8);
        } catch (Exception e) {
            throw new KeyStoreException(new Throwable(e.getMessage()));
        }
    }

    private RSAPublicKey getPublicKey() throws UnrecoverableEntryException, NoSuchAlgorithmException, java.security.KeyStoreException {
        final KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) this.keyStore.getEntry(this.alias, null);
        return (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
    }

    private PrivateKey getPrivateKey() throws UnrecoverableEntryException, NoSuchAlgorithmException, java.security.KeyStoreException {
        final KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) this.keyStore.getEntry(this.alias, null);
        return privateKeyEntry.getPrivateKey();
    }
}
