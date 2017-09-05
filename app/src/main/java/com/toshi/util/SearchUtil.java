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

public class SearchUtil {

    /**
     * Returns null if no match
     * @param searchList The list to search in
     * @param itemToFind The item to look for
     * @return The matching item of type T
     */
    public static <T extends Comparable<T>> T findMatch(final List<T> searchList, final T itemToFind) {
        final int searchResult = Collections.binarySearch(searchList, itemToFind);
        if (Math.abs(searchResult) >= searchList.size() || searchResult < 0) return null;
        return searchList.get(searchResult);
    }

    /**
     * Returns null if no suggestion is found
     * @param searchList The list to search in
     * @param itemToFind The item to look for
     * @return The suggestion of type String
     */
    public static String findStringSuggestion(final List<String> searchList, final String itemToFind) {
        final int searchResult = Collections.binarySearch(searchList, itemToFind);
        final int absoluteIndex = Math.abs(searchResult);

        //If the insertion point is >= searchList.size(), compare it to the last item
        if (absoluteIndex >= searchList.size()) {
            final String suggestion = searchList.get(searchList.size() - 1);
            return suggestion.startsWith(itemToFind) ? suggestion : null;
        }

        final int index = searchResult < 0 ? absoluteIndex - 1 : searchResult;
        final String suggestion = searchList.get(index);
        return suggestion != null && suggestion.startsWith(itemToFind) ? suggestion : null;
    }
}
