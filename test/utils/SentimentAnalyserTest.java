package utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link SentimentAnalyser} class.
 *
 * <p>This class tests the functionality of sentiment analysis methods,
 * including loading keywords from files, analyzing individual descriptions,
 * and calculating overall sentiment from a list of descriptions.</p>
 */
public class SentimentAnalyserTest extends WithApplication {

    private SentimentAnalyser sentimentAnalyser;
    private Path happyKeywordsFile;
    private Path sadKeywordsFile;

    /**
     * Sets up the test environment by initializing the {@link SentimentAnalyser}
     * with temporary keyword files for testing.
     *
     * @throws IOException if an error occurs while creating temporary files
     */
    @Before
    public void setUp() throws IOException {
        happyKeywordsFile = Files.createTempFile("happy_keywords", ".txt");
        sadKeywordsFile = Files.createTempFile("sad_keywords", ".txt");

        Files.write(happyKeywordsFile, List.of("joy", "delightful", "happy", "exciting"));
        Files.write(sadKeywordsFile, List.of("sad", "depressing", "miserable", "unhappy"));

        List<String> happyKeywords = Files.readAllLines(happyKeywordsFile);
        List<String> sadKeywords = Files.readAllLines(sadKeywordsFile);

        sentimentAnalyser = new SentimentAnalyser(happyKeywords, sadKeywords);
    }

    /**
     * Cleans up the test environment by deleting temporary keyword files.
     *
     * @throws IOException if an error occurs while deleting temporary files
     */
    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(happyKeywordsFile);
        Files.deleteIfExists(sadKeywordsFile);
    }

    /**
     * Tests loading keywords from valid files for happy and sad keywords.
     */
    @Test
    public void testLoadKeywordsFromFileSuccess() {
        List<String> happyKeywords = SentimentAnalyser.loadKeywordsFromFile(happyKeywordsFile.toString());
        List<String> sadKeywords = SentimentAnalyser.loadKeywordsFromFile(sadKeywordsFile.toString());

        assertEquals(List.of("joy", "delightful", "happy", "exciting"), happyKeywords);
        assertEquals(List.of("sad", "depressing", "miserable", "unhappy"), sadKeywords);
    }

    /**
     * Tests loading keywords from a non-existent file, expecting an empty list.
     */
    @Test
    public void testLoadKeywordsFromFileNotFound() {
        List<String> result = SentimentAnalyser.loadKeywordsFromFile("conf/non_existent_file.txt");
        assertTrue(result.isEmpty());
    }

    /**
     * Tests loading keywords from an empty file, expecting an empty list.
     *
     * @throws IOException if an error occurs while creating or deleting the temporary file
     */
    @Test
    public void testLoadKeywordsFromFileEmpty() throws IOException {
        Path emptyFile = Files.createTempFile("empty_keywords", ".txt");
        List<String> result = SentimentAnalyser.loadKeywordsFromFile(emptyFile.toString());

        assertTrue(result.isEmpty());
        Files.deleteIfExists(emptyFile);
    }

    /**
     * Tests analyzing a description with happy sentiment keywords.
     */
    @Test
    public void testAnalyzeDescriptionHappySentiment() {
        String description = "This is a wonderful and joyous day, full of happiness and excitement!";
        String result = sentimentAnalyser.analyzeDescription(description);
        assertEquals(":-)", result);
    }

    /**
     * Tests analyzing a description with sad sentiment keywords.
     */
    @Test
    public void testAnalyzeDescriptionSadSentiment() {
        String description = "It’s such a miserable and depressing day. Nothing feels good, everything is sad.";
        String result = sentimentAnalyser.analyzeDescription(description);
        assertEquals(":-(", result);
    }

    /**
     * Tests analyzing a description with neutral sentiment.
     */
    @Test
    public void testAnalyzeDescriptionNeutralSentiment() {
        String description = "Today is just another ordinary day, nothing much happened.";
        String result = sentimentAnalyser.analyzeDescription(description);
        assertEquals(":-|", result);
    }

    /**
     * Tests analyzing overall sentiment for a list of mostly happy descriptions.
     */
    @Test
    public void testAnalyzeOverallSentimentMostlyHappy() {
        List<String> descriptions = List.of(
                "Such a delightful experience, I feel great joy!",
                "I am thrilled and happy with how things went.",
                "This is a wonderful moment of happiness."
        );
        String result = sentimentAnalyser.analyzeOverallSentiment(descriptions);
        assertEquals(":-|", result);
    }

    /**
     * Tests analyzing overall sentiment for a list of mostly sad descriptions.
     */
    @Test
    public void testAnalyzeOverallSentimentMostlySad() {
        List<String> descriptions = List.of(
                "Such a miserable day, everything is disappointing.",
                "Feeling really low and unhappy with what happened.",
                "It was a sad and depressing experience."
        );
        String result = sentimentAnalyser.analyzeOverallSentiment(descriptions);
        assertEquals(":-|", result);
    }

    /**
     * Tests analyzing overall sentiment for a mixed list of descriptions.
     */
    @Test
    public void testAnalyzeOverallSentimentMixedSentiments() {
        List<String> descriptions = List.of(
                "This experience was quite disappointing and sad.",
                "I'm delighted with the results, feeling joyous.",
                "It's just a regular day, nothing too exciting."
        );
        String result = sentimentAnalyser.analyzeOverallSentiment(descriptions);
        assertEquals(":-|", result);
    }

    /**
     * Tests analyzing an empty description, expecting a neutral sentiment.
     */
    @Test
    public void testAnalyzeDescriptionEmptyString() {
        String description = "";
        String result = sentimentAnalyser.analyzeDescription(description);
        assertEquals(":-|", result);
    }

    /**
     * Tests analyzing a null description, expecting a neutral sentiment.
     */
    @Test
    public void testAnalyzeDescriptionNull() {
        String result = sentimentAnalyser.analyzeDescription(null);
        assertEquals(":-|", result);
    }

    /**
     * Tests analyzing an empty list of descriptions, expecting a neutral overall sentiment.
     */
    @Test
    public void testAnalyzeOverallSentimentEmptyList() {
        List<String> descriptions = List.of();
        String result = sentimentAnalyser.analyzeOverallSentiment(descriptions);
        assertEquals(":-|", result);
    }
}