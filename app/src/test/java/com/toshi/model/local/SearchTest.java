package com.toshi.model.local;

import com.toshi.util.SearchUtil;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SearchTest {

    private final List<String> searchList = Arrays.asList("abandon", "ability", "circle", "dad", "happy",
            "hello", "homeless", "man", "quit", "rocket", "shine", "shiver", "square", "tower", "zebra");

    @Test
    public void testMatches() {
        final String[] validWords = new String[]{"zebra", "abandon"};
        final String[] invalidWords = new String[]{"nothing", "hallo", "class"};

        for (final String word : validWords) assertHasWord(word);
        for (final String word : invalidWords) assertNoMatch(word);
    }

    private void assertHasWord(final String word) {
        assertThat(word, is(SearchUtil.findMatch(searchList, word)));
    }

    private void assertNoMatch(final String word) {
        assertThat(null, is(SearchUtil.findMatch(searchList, word)));
    }

    @Test
    public void testSuggestions() {
        final String[] validWords = new String[]{"ze", "ab", "roc"};
        final String[] invalidWords = new String[]{"noth", "wa", "cl"};

        assertThat("zebra", is(SearchUtil.findStringSuggestion(searchList, validWords[0])));
        assertThat("abandon", is(SearchUtil.findStringSuggestion(searchList, validWords[1])));
        for (final String word : invalidWords) assertNoSuggestion(word);
    }

    private void assertNoSuggestion(final String word) {
        assertThat(null, is(SearchUtil.findStringSuggestion(searchList, word)));
    }
}
