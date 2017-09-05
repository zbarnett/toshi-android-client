package com.toshi.QrCode;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.toshi.util.QrCode;
import com.toshi.util.QrCodeType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class QrCodeTest {

    @Test
    public void testEthereumPrefixPaymentAddressQrCode() {
        final QrCode qrCode = new QrCode("ethereum:0x25d2c9851b793f7256b51f39513b23063e8835c9");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.PAYMENT_ADDRESS));
    }

    @Test
    public void testTooShortEthereumPrefixPaymentAddressQrCode() {
        final QrCode qrCode = new QrCode("ethereum:0x25d2c9851b751f39513b23063e8835c9");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.INVALID));
    }

    @Test
    public void testIbanPrefixPaymentAddressQrCode() {
        final QrCode qrCode = new QrCode("iban:XE7338O073KYGTWWZN0F2WZ0R8PX5ZPPZS");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.PAYMENT_ADDRESS));
    }

    @Test
    public void testTooShortIbanPrefixPaymentAddressQrCode() {
        final QrCode qrCode = new QrCode("iban:XE7338O073KYN0F2WZ0R8X5ZPPZS");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.INVALID));
    }

    @Test
    public void testAddQrCode() {
        final QrCode qrCode = new QrCode("https://app.toshi.org/add/@user16780");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.ADD));
    }

    @Test
    public void testAddQrCodeWithoutUsername() {
        final QrCode qrCode = new QrCode("https://app.toshi.org/add/");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.INVALID));
    }

    @Test
    public void testAddQrCodeWithoutUrlType() {
        final QrCode qrCode = new QrCode("https://app.toshi.org/@user16780");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.INVALID));
    }

    @Test
    public void testPayQrCode() {
        final QrCode qrCode = new QrCode("https://app.toshi.org/pay/@user16780?value=10000000000000000&memo=Hello!");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.PAY));
    }

    @Test
    public void testPaymentQrCodeWithoutMemo() {
        final QrCode qrCode = new QrCode("https://app.toshi.org/pay/@user16780?value=10000000000000000");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.PAY));
    }

    @Test
    public void testPayQrCodeWithoutAmount() {
        final QrCode qrCode = new QrCode("https://app.toshi.org/pay/@user16780");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.INVALID));
    }

    @Test
    public void testPayQrCodeWithoutUrlType() {
        final QrCode qrCode = new QrCode("https://app.toshi.org/@user16780?value=10000000000000000");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.INVALID));
    }

    @Test
    public void testPayQrCodeWithoutUsername() {
        final QrCode qrCode = new QrCode("https://app.toshi.org/pay?value=10000000000000000");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.INVALID));
    }

    @Test
    public void testExternalPaymentQrCode() {
        final QrCode qrCode = new QrCode("ethereum:0x3c7d8412ebf9940bed19f573eba52ba9a0a05bff?value=10000000000000000&memo=Hello!");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.EXTERNAL_PAY));
    }

    @Test
    public void testExternalPaymentQrCodeWithoutMemo() {
        final QrCode qrCode = new QrCode("ethereum:0x3c7d8412ebf9940bed19f573eba52ba9a0a05bff?value=10000000000000000");
        assertThat(qrCode.getQrCodeType(), is(QrCodeType.EXTERNAL_PAY));
    }
}
