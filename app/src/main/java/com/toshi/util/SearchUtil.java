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

import java.util.Collections;
import java.util.List;

public class SearchUtil<T extends Comparable<T>> {

    /**
     * Returns null if no match
     * @param searchList The list to search in
     * @param itemToFind The item to look for
     * @return The matching item of type T
     */
    public T findMatch(final List<T> searchList, final T itemToFind) {
        final int searchResult = Collections.binarySearch(searchList, itemToFind);
        if (Math.abs(searchResult) >= searchList.size() || searchResult < 0) return null;
        return searchList.get(searchResult);
    }

    /**
     * Returns null if no suggestion is found
     * @param searchList The list to search in
     * @param itemToFind The item to look for
     * @return The suggestion of type T
     */
    public T findSuggestion(final List<T> searchList, final T itemToFind) {
        final int searchResult = Collections.binarySearch(searchList, itemToFind);
        if (Math.abs(searchResult) >= searchList.size()) return null;
        final int index = searchResult < 0 ? Math.abs(searchResult) - 1 : searchResult;
        return searchList.get(index);
    }
}
