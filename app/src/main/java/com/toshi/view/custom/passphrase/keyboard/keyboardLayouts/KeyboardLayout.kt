/*
 * Copyright (c) 2017. Toshi Inc
 *
 *  This program is free software: you can redistribute it and/or modify
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

package com.toshi.view.custom.passphrase.keyboard.keyboardLayouts

abstract class KeyboardLayout {

    enum class Action {
        BACKSPACE,
        SHIFT,
        SPACEBAR,
        RETURN,
        LANGUAGE
    }

    enum class Layout {
        QWERTY,
        ABCDEF,
        QWERTZ,
        AZERTY;
    }

    companion object {
        const val FIRST_ROW = 0
        const val SECOND_ROW = 1
        const val THIRD_ROW = 2
        const val FOURTH_ROW = 3

        fun getLayout(layout: Layout): KeyboardLayout {
            return when (layout) {
                Layout.QWERTY -> Qwerty()
                Layout.ABCDEF -> Abcdef()
                Layout.QWERTZ -> Qwertz()
                Layout.AZERTY -> Azerty()
            }
        }
    }

    abstract fun isLastChatOnRow(key: Any): Boolean
    abstract fun getLayout(): List<Row>

    class Qwerty : KeyboardLayout() {
        override fun getLayout() = listOf(
                Row(listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")),
                Row(listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")),
                Row(listOf(Action.SHIFT, "z", "x", "c", "v", "b", "n", "m", Action.BACKSPACE)),
                Row(listOf(Action.LANGUAGE, Action.SPACEBAR, Action.RETURN))
        )

        override fun isLastChatOnRow(key: Any): Boolean {
            return key == "p" || key == "l" || key == Action.BACKSPACE || key == Action.RETURN
        }
    }

    class Abcdef : KeyboardLayout() {
        override fun getLayout() = listOf(
                Row(listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")),
                Row(listOf("k", "l", "m", "n", "o", "p", "q", "r", "s")),
                Row(listOf(Action.SHIFT, "t", "u", "v", "w", "x", "y", "z", Action.BACKSPACE)),
                Row(listOf(Action.LANGUAGE, Action.SPACEBAR, Action.RETURN))
        )

        override fun isLastChatOnRow(key: Any): Boolean {
            return key == "j" || key == "s" || key == Action.BACKSPACE || key == Action.RETURN
        }
    }

    class Qwertz : KeyboardLayout() {
        override fun getLayout() = listOf(
                Row(listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p")),
                Row(listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")),
                Row(listOf(Action.SHIFT, "y", "x", "c", "v", "b", "n", "m", Action.BACKSPACE)),
                Row(listOf(Action.LANGUAGE, Action.SPACEBAR, Action.RETURN))
        )

        override fun isLastChatOnRow(key: Any): Boolean {
            return key == "p" || key == "l" || key == Action.BACKSPACE || key == Action.RETURN
        }
    }

    class Azerty : KeyboardLayout() {
        override fun getLayout() = listOf(
                Row(listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p")),
                Row(listOf("q", "s", "d", "f", "g", "h", "j", "k", "l")),
                Row(listOf(Action.SHIFT, "m", "w", "x", "c", "v", "b", "n", Action.BACKSPACE)),
                Row(listOf(Action.LANGUAGE, Action.SPACEBAR, Action.RETURN))
        )

        override fun isLastChatOnRow(key: Any): Boolean {
            return key == "p" || key == "l" || key == Action.BACKSPACE || key == Action.RETURN
        }
    }
}

data class Row(
        val list: List<Any> = emptyList()
)