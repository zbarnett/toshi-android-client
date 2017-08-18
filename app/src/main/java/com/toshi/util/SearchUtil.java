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
