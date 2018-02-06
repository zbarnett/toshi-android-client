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

package com.toshi.util;

import com.toshi.view.activity.webView.HeaderParser;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HeaderParserTest {

    private HeaderParser headerParser = new HeaderParser();

    @Test
    public void assertUtf8Charset() {
        final String charset = "utf-8";
        final String contentType = "text/html; charset=" + charset;
        final String charsetResult = headerParser.getCharset(contentType);
        assertThat(charset, is(charsetResult));
    }

    @Test
    public void assertIsoCharset() {
        final String charset = "ISO-8859-1";
        final String contentType = "text/html; charset=" + charset;
        final String charsetResult = headerParser.getCharset(contentType);
        assertThat(charset, is(charsetResult));
    }

    @Test
    public void assertEmptyCharset() {
        final String charset = "";
        final String contentType = "text/html" + charset;
        final String charsetResult = headerParser.getCharset(contentType);
        assertThat(HeaderParser.DEFAULT_CHARSET, is(charsetResult));
    }

    @Test
    public void assertAbsentCharset() {
        final String contentType = "text/html";
        final String charsetResult = headerParser.getCharset(contentType);
        assertThat(HeaderParser.DEFAULT_CHARSET, is(charsetResult));
    }

    @Test
    public void assertHtmlMimeType() {
        final String mimeType = "text/html";
        final String contentType = mimeType + "; charset=utf-8";
        final String mimeTypeResult = headerParser.getMimeType(contentType);
        assertThat(mimeType, is(mimeTypeResult));
    }

    @Test
    public void assertJsonMimeType() {
        final String mimeType = "application/json";
        final String contentType = mimeType + "; charset=utf-8";
        final String mimeTypeResult = headerParser.getMimeType(contentType);
        assertThat(mimeType, is(mimeTypeResult));
    }

    @Test
    public void assertAbsentMimeType() {
        final String contentType = "charset=utf-8";
        final String mimeTypeResult = headerParser.getMimeType(contentType);
        assertThat(HeaderParser.DEFAULT_MIME_TYPE, is(mimeTypeResult));
    }
}
