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

package com.toshi.util.logging;


import timber.log.Timber;

public class LogUtil  {

    private LogUtil() {}

    // More verbose wrapper functions

    @SuppressWarnings("rawtypes")
    public static void print(final String message) {
        Timber.i(message);
    }

    // Wrappers that allow to be called with a class

    @SuppressWarnings("rawtypes")
    public static void d(final String message) {
        Timber.d(message);
    }

    @SuppressWarnings("rawtypes")
    public static void w(final String message) {
        Timber.e(message);
    }

    @SuppressWarnings("rawtypes")
    public static void exception(final Throwable throwable) {
        Timber.e(throwable);
    }

    @SuppressWarnings("rawtypes")
    public static void exception(final String additionalInfo, final Throwable throwable) {
        Timber.e(throwable, additionalInfo);
    }

    @SuppressWarnings("rawtypes")
    public static void exception(final String additionalInfo) {
        Timber.e(additionalInfo);
    }

    @SuppressWarnings("rawtypes")
    public static void i(final String message) {
        Timber.i(message);
    }

    @SuppressWarnings("rawtypes")
    public static void v(final String message) {
        Timber.v(message);
    }

    @SuppressWarnings("rawtypes")
    public static void wtf(final String message) {
        Timber.wtf(message);
    }
}
