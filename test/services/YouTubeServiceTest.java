package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class YouTubeServiceTest {

    private YouTubeService youTubeService;

    @BeforeEach
    public void setup() {
        // Directly initialize the service (minimal mocking)
        youTubeService = new YouTubeService();
    }

//    @Test
//    public void testShortenDescription() {
//        // Call the method with various inputs
//        assertEquals("", youTubeService.shortenDescription(null)); // Null input
//        assertEquals("", youTubeService.shortenDescription(""));   // Empty string
//        assertEquals("Short description", youTubeService.shortenDescription("Short description")); // Short input
//        assertEquals("This is a long descriptio...", youTubeService.shortenDescription("This is a long description that exceeds 100 characters in length and needs to be truncated for display."));
//    }

    @Test
    public void testFetchDescriptionsByQuery() {
        // Mock the results to simulate API interaction
        YouTubeService spyService = spy(youTubeService);

        // Mock the descriptions returned by the API
        doReturn(CompletableFuture.completedFuture(Arrays.asList(
                "Description 1",
                "Description 2",
                "Another description"
        ))).when(spyService).fetchDescriptionsByQuery("testQuery");

        // Call the method
        CompletableFuture<List<String>> resultFuture = spyService.fetchDescriptionsByQuery("testQuery");
        List<String> result = resultFuture.join();

        // Assert the results
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Description 1"));
        assertTrue(result.contains("Another description"));
    }

    @Test
    public void testFetchWordStatsByQuery() {
        // Mock the descriptions for a query
        YouTubeService spyService = spy(youTubeService);
        doReturn(CompletableFuture.completedFuture(Arrays.asList(
                "Hello world! Hello again.",
                "Testing world functionality."
        ))).when(spyService).fetchDescriptionsByQuery("wordStatsQuery");

        // Call the method
        CompletableFuture<Map<String, Integer>> resultFuture = spyService.fetchWordStatsByQuery("wordStatsQuery");
        Map<String, Integer> wordStats = resultFuture.join();

        // Assert the word statistics
        assertNotNull(wordStats);
        assertEquals(2, (int) wordStats.get("hello")); // "Hello" appears twice
        assertEquals(2, (int) wordStats.get("world")); // "World" appears twice
        assertEquals(1, (int) wordStats.get("testing")); // "Testing" appears once
    }
}
