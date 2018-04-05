/*
 * 	Copyright (c) 2018. Toshi Inc
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

package com.toshi.adapter

import android.content.Context
import android.support.test.runner.AndroidJUnit4
import android.support.test.rule.ActivityTestRule
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.toshi.view.activity.SplashActivity
import com.toshi.view.adapter.CompoundAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompoundAdapterTests {

    @Rule @JvmField
    val activityRule = ActivityTestRule(SplashActivity::class.java)

    private val stringList = listOf(
            "working",
            "around",
            "kotlin's",
            "type",
            "system"
    )

    private val intList = listOf(
            0,
            1,
            2
    )

    private val intAdapter = IntCompoundableAdapter(intList)
    private val stringAdapter = StringCompoundableAdapter(stringList)
    private val testCompoundAdapter = CompoundAdapter(listOf(
            intAdapter,
            stringAdapter
    ))
    private val initialCompoundSize = intList.size + stringList.size

    private fun context(): Context {
        return activityRule.activity
    }

    @Test
    fun compoundAdapterHasCorrectTotalNumberOfItems() {
        assertEquals(initialCompoundSize, testCompoundAdapter.itemCount)
    }

    @Test
    fun compoundAdapterReturnsCorrectViewHolderForSectionIndex() {
        val recyclerView = RecyclerView(context())
        recyclerView.adapter = testCompoundAdapter

        val indexOfIntAdapter = testCompoundAdapter.indexOf(intAdapter)
        if (indexOfIntAdapter == null) {
            fail("Int section index was null")
            return
        }
        assertEquals(0, indexOfIntAdapter)

        val shouldBeAnIntViewHolder = testCompoundAdapter.onCreateViewHolder(recyclerView, indexOfIntAdapter)
        assertTrue(shouldBeAnIntViewHolder is IntViewHolder)

        val indexOfStringAdapter = testCompoundAdapter.indexOf(stringAdapter)
        if (indexOfStringAdapter == null) {
            fail("String section index was null")
            return
        }
        assertEquals(1, indexOfStringAdapter)

        val shouldBeAStringViewHolder = testCompoundAdapter.onCreateViewHolder(recyclerView, indexOfStringAdapter)
        assertTrue(shouldBeAStringViewHolder is StringViewHolder)
    }

    @Test
    fun compoundAdapterReturnsCorrectConfiguredViewForPosition() {
        val recyclerView = RecyclerView(context())
        recyclerView.adapter = testCompoundAdapter
        recyclerView.layoutManager = LinearLayoutManager(context())

        val intSectionIndex = testCompoundAdapter.indexOf(intAdapter)
        if (intSectionIndex == null) {
            fail("Section index was null")
            return
        }
        val intViewHolder = testCompoundAdapter.onCreateViewHolder(recyclerView, intSectionIndex)
        testCompoundAdapter.onBindViewHolder(intViewHolder, intList.lastIndex)

        val intView = intViewHolder as? IntViewHolder
        if (intView == null) {
            fail("Int view not correct type!")
            return
        }
        assertEquals("${intList.last()}", intView.textView.text)

        val stringSectionIndex = testCompoundAdapter.indexOf(stringAdapter)
        if (stringSectionIndex == null) {
            fail("String section index was null")
            return
        }

        val stringViewHolder = testCompoundAdapter.onCreateViewHolder(recyclerView, stringSectionIndex)
        val firstStringIndex = intList.size
        testCompoundAdapter.onBindViewHolder(stringViewHolder, firstStringIndex)

        val stringView = stringViewHolder as? StringViewHolder
        if (stringView == null) {
            fail("Couldn't get correct type to check for string view holder")
            return
        }
        assertEquals(stringList.first(), stringView.textView.text)
    }

    @Test
    fun addingAnotherAdapterUpdatesCount() {
        assertEquals(initialCompoundSize, testCompoundAdapter.itemCount)

        val anotherIntAdapter = IntCompoundableAdapter(intList)
        testCompoundAdapter.appendAdapter(anotherIntAdapter)

        assertEquals((initialCompoundSize + intList.size), testCompoundAdapter.itemCount)
        assertEquals(testCompoundAdapter.indexOf(anotherIntAdapter), 2)
    }

    @Test
    fun addingAnotherAdapterUpdatesCountAndIndex() {
        assertEquals(initialCompoundSize, testCompoundAdapter.itemCount)

        val anotherStringAdapter = StringCompoundableAdapter(stringList)
        testCompoundAdapter.insertAdapter(anotherStringAdapter, 0)

        assertEquals((initialCompoundSize + stringList.size), testCompoundAdapter.itemCount)

        assertEquals(0, testCompoundAdapter.indexOf(anotherStringAdapter))
        assertEquals(2, testCompoundAdapter.indexOf(stringAdapter))
    }

    @Test
    fun testRemovingAnAdapterUpdatesCountAndIndex() {
        assertEquals(initialCompoundSize, testCompoundAdapter.itemCount)

        testCompoundAdapter.removeAdapter(stringAdapter)

        assertEquals((initialCompoundSize - stringList.size), testCompoundAdapter.itemCount)
        assertNull(testCompoundAdapter.indexOf(stringAdapter))
    }

    @Test
    fun testAddingEmptyAdapterWorks() {
        assertEquals(initialCompoundSize, testCompoundAdapter.itemCount)

        val emptyIntAdapter = IntCompoundableAdapter(listOf())

        testCompoundAdapter.insertAdapter(emptyIntAdapter, 0)

        // We haven't actually added any items, so this shouldn't change the count.
        assertEquals(initialCompoundSize, testCompoundAdapter.itemCount)

        assertEquals(0, testCompoundAdapter.indexOf(emptyIntAdapter))
        assertEquals(1, testCompoundAdapter.indexOf(intAdapter))
        assertEquals(2, testCompoundAdapter.indexOf(stringAdapter))

        val recyclerView = RecyclerView(context())
        recyclerView.adapter = testCompoundAdapter
        recyclerView.layoutManager = LinearLayoutManager(context())

        val shouldBeIntViewHolder = testCompoundAdapter.onCreateViewHolder(recyclerView, 0)
        assertTrue(shouldBeIntViewHolder is IntViewHolder)

        val shouldBeAnotherIntViewHolder = testCompoundAdapter.onCreateViewHolder(recyclerView, 1)
        val shouldBeStringViewHolder = testCompoundAdapter.onCreateViewHolder(recyclerView, 2)

        testCompoundAdapter.onBindViewHolder(shouldBeAnotherIntViewHolder, 0)
        val intView = shouldBeAnotherIntViewHolder as? IntViewHolder
        if (intView == null) {
            fail("Another int view holder not correct type!")
            return
        }

        assertEquals("${intList.first()}", intView.textView.text)

        testCompoundAdapter.onBindViewHolder(shouldBeStringViewHolder, intList.count())

        val stringView = shouldBeStringViewHolder as? StringViewHolder
        if (stringView == null) {
            fail("Wrong type for string view holder!")
            return
        }

        assertEquals(stringList.first(), stringView.textView.text)
    }
}