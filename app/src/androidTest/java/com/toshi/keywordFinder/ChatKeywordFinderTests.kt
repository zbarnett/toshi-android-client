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

package com.toshi.keywordFinder

import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.toshi.util.spannables.ChatKeywordFinder
import com.toshi.util.spannables.Keyword
import com.toshi.util.spannables.KeywordType
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ChatKeywordFinderTests {

    val pattern by lazy { ChatKeywordFinder.getKeywordPattern() }

    @Test
    fun checkTextWithoutProtocolIsValid() {
        val pattern = ChatKeywordFinder.getKeywordPattern()
        val testTextList = getListOfDifferentTextWithDifferentUrlFormats()
        val textWithoutUrlProtocol = testTextList[0]
        val keywords = ChatKeywordFinder.findKeywords(textWithoutUrlProtocol.testText, pattern)
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].text, `is`(keywords[0].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].type, `is`(keywords[0].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].text, `is`(keywords[1].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].type, `is`(keywords[1].type))
    }

    @Test
    fun checkTextWithHttpsIsValid() {
        val testTextList = getListOfDifferentTextWithDifferentUrlFormats()
        val textWithoutUrlProtocol = testTextList[1]
        val keywords = ChatKeywordFinder.findKeywords(textWithoutUrlProtocol.testText, pattern)
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].text, `is`(keywords[0].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].type, `is`(keywords[0].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].text, `is`(keywords[1].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].type, `is`(keywords[1].type))
    }

    @Test
    fun checkTextWithHttpIsValid() {
        val testTextList = getListOfDifferentTextWithDifferentUrlFormats()
        val textWithoutUrlProtocol = testTextList[2]
        val keywords = ChatKeywordFinder.findKeywords(textWithoutUrlProtocol.testText, pattern)
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].text, `is`(keywords[0].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].type, `is`(keywords[0].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].text, `is`(keywords[1].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].type, `is`(keywords[1].type))
    }

    @Test
    fun checkTextWithoutProtolAndWwwIsValid() {
        val testTextList = getListOfDifferentTextWithDifferentUrlFormats()
        val textWithoutUrlProtocol = testTextList[3]
        val keywords = ChatKeywordFinder.findKeywords(textWithoutUrlProtocol.testText, pattern)
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].text, `is`(keywords[0].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].type, `is`(keywords[0].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].text, `is`(keywords[1].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].type, `is`(keywords[1].type))
    }

    @Test
    fun checkTextWithNormalUsername() {
        val testTextList = getListOfUsernameTestString()
        val textWithoutUrlProtocol = testTextList[0]
        val keywords = ChatKeywordFinder.findKeywords(textWithoutUrlProtocol.testText, pattern)
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].text, `is`(keywords[0].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].type, `is`(keywords[0].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].text, `is`(keywords[1].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].type, `is`(keywords[1].type))
    }

    @Test
    fun checkTextWithEmailAddress() {
        val testTextList = getListOfUsernameTestString()
        val textWithoutUrlProtocol = testTextList[1]
        val keywords = ChatKeywordFinder.findKeywords(textWithoutUrlProtocol.testText, pattern)
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].text, `is`(keywords[0].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].type, `is`(keywords[0].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].text, `is`(keywords[1].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].type, `is`(keywords[1].type))
    }

    @Test
    fun checkTextWithMultipleUsernames() {
        val testTextList = getListOfUsernameTestString()
        val textWithoutUrlProtocol = testTextList[2]
        val keywords = ChatKeywordFinder.findKeywords(textWithoutUrlProtocol.testText, pattern)
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].text, `is`(keywords[0].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[0].type, `is`(keywords[0].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].text, `is`(keywords[1].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[1].type, `is`(keywords[1].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[2].text, `is`(keywords[2].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[2].type, `is`(keywords[2].type))
        assertThat(textWithoutUrlProtocol.expectedKeywords[3].text, `is`(keywords[3].text))
        assertThat(textWithoutUrlProtocol.expectedKeywords[3].type, `is`(keywords[3].type))
    }

    private fun getListOfDifferentTextWithDifferentUrlFormats(): List<KeywordTestData> {
        return listOf(
                KeywordTestData(
                        "Hi! Sap @user1234, check out toshi at www.toshi.com",
                        listOf(
                                Keyword("@user1234", KeywordType.USERNAME),
                                Keyword("www.toshi.com", KeywordType.WEB_URL)
                        )
                ),
                KeywordTestData(
                        "Hi! Sap @user1234, check out toshi at https://www.toshi.com",
                        listOf(
                                Keyword("@user1234", KeywordType.USERNAME),
                                Keyword("https://www.toshi.com", KeywordType.WEB_URL)
                        )
                ),
                KeywordTestData(
                        "Hi! Sap @user1234, check out toshi at http://www.toshi.com",
                        listOf(
                                Keyword("@user1234", KeywordType.USERNAME),
                                Keyword("http://www.toshi.com", KeywordType.WEB_URL)
                        )
                ),
                KeywordTestData(
                        "Hi! Sap @user1234, check out toshi at toshi.com",
                        listOf(
                                Keyword("@user1234", KeywordType.USERNAME),
                                Keyword("toshi.com", KeywordType.WEB_URL)
                        )
                ),
                KeywordTestData(
                        "https://toshi.com, check it out @user1234!",
                        listOf(
                                Keyword("toshi.com", KeywordType.WEB_URL),
                                Keyword("@user1234", KeywordType.USERNAME)
                        )
                )
        )
    }

    private fun getListOfUsernameTestString(): List<KeywordTestData> {
        return listOf(
                KeywordTestData(
                        "Hi! Sap @user41231, check out toshi at www.toshi.com",
                        listOf(
                                Keyword("@user41231", KeywordType.USERNAME),
                                Keyword("www.toshi.com", KeywordType.WEB_URL)
                        )
                ),
                KeywordTestData(
                        "Hi! Sap user@gmail.com, check out toshi at www.toshi.com",
                        listOf(
                                Keyword("gmail.com", KeywordType.WEB_URL),
                                Keyword("www.toshi.com", KeywordType.WEB_URL)
                        )
                ),
                KeywordTestData(
                        "Hi! Sap @user1234, @testUser and @USER check out toshi at www.toshi.com",
                        listOf(
                                Keyword("@user1234", KeywordType.USERNAME),
                                Keyword("@testUser", KeywordType.USERNAME),
                                Keyword("@USER", KeywordType.USERNAME),
                                Keyword("www.toshi.com", KeywordType.WEB_URL)
                        )
                )
        )
    }
}