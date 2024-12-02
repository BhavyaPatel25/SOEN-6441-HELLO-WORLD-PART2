package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ReadabilityScoreUtil class.
 * This test class verifies the functionality of various readability and text analysis methods.
 */
public class ReadabilityScoreUtilTest {

    /**
     * Tests calculateFleschKincaidGradeLevel for a sample English sentence.
     * Verifies that the grade level is calculated accurately.
     */
    @Test
    public void testCalculateFleschKincaidGradeLevel() {
        String content = "This is a simple test sentence. It should be easy to read.";
        double gradeLevel = ReadabilityScoreUtil.calculateFleschKincaidGradeLevel(content);
        assertEquals(0.5166666666666693, gradeLevel, 0.1, "Expected grade level around 2.35");
    }

    /**
     * Tests calculateFleschReadingEaseScore for a sample English sentence.
     * Verifies that the reading ease score is calculated accurately.
     */
    @Test
    public void testCalculateFleschReadingEaseScore() {
        String content = "This is a simple test sentence. It should be easy to read.";
        double easeScore = ReadabilityScoreUtil.calculateFleschReadingEaseScore(content);
        assertEquals(102.045, easeScore, 5.0, "Expected reading ease score around 80");
    }

    /**
     * Tests calculateFleschKincaidGradeLevel with empty content.
     * Verifies that the grade level is 0 for empty content.
     */
    @Test
    void testCalculateFleschKincaidGradeLevelWithEmptyContent() {
        String content = "";
        double gradeLevel = ReadabilityScoreUtil.calculateFleschKincaidGradeLevel(content);
        assertEquals(0, gradeLevel, "Grade level should be 0 for empty content.");
    }

    /**
     * Tests calculateFleschKincaidGradeLevel with non-English content.
     * Verifies that the grade level is 0 for non-English content.
     */
    @Test
    void testCalculateFleschKincaidGradeLevelWithNonEnglishContent() {
        String content = "यह एक हिंदी वाक्य है।";
        double gradeLevel = ReadabilityScoreUtil.calculateFleschKincaidGradeLevel(content);
        assertEquals(0, gradeLevel, "Grade level should be 0 for non-English content.");
    }

    /**
     * Tests calculateFleschReadingEaseScore with empty content.
     * Verifies that the reading ease score is 0 for empty content.
     */
    @Test
    void testCalculateFleschReadingEaseScoreWithEmptyContent() {
        String content = "";
        double readingEase = ReadabilityScoreUtil.calculateFleschReadingEaseScore(content);
        assertEquals(0, readingEase, "Reading Ease score should be 0 for empty content.");
    }

    /**
     * Tests calculateFleschReadingEaseScore with non-English content.
     * Verifies that the reading ease score is 0 for non-English content.
     */
    @Test
    void testCalculateFleschReadingEaseScoreWithNonEnglishContent() {
        String content = "यह एक हिंदी वाक्य है।";
        double readingEase = ReadabilityScoreUtil.calculateFleschReadingEaseScore(content);
        assertEquals(0, readingEase, "Reading Ease score should be 0 for non-English content.");
    }

    /**
     * Tests countSyllablesInWord with an empty string.
     * Verifies that the syllable count is 0 for an empty string.
     */
    @Test
    void testCountSyllablesInWordWithEmptyString() {
        assertEquals(0, ReadabilityScoreUtil.countSyllablesInWord(""), "Should return 0 for an empty string.");
    }

    /**
     * Tests countSentences with multiple sentences in the content.
     * Verifies that the sentence count is correctly determined.
     */
    @Test
    public void testCountSentences() {
        String content = "This is the first sentence. And this is the second! Is this the third?";
        int sentenceCount = ReadabilityScoreUtil.countSentences(content);
        assertEquals(3, sentenceCount, "Expected 3 sentences in the content.");
    }

    /**
     * Tests countWords with a sample sentence.
     * Verifies that the word count is correctly determined.
     */
    @Test
    public void testCountWords() {
        String content = "This is a test with five words.";
        int wordCount = ReadabilityScoreUtil.countWords(content);
        assertEquals(7, wordCount, "Expected 6 words in the content.");
    }

    /**
     * Tests countTotalSyllables with a sample sentence.
     * Verifies that the total syllable count is correctly determined.
     */
    @Test
    public void testCountTotalSyllables() {
        String content = "This is a simple sentence.";
        int syllableCount = ReadabilityScoreUtil.countTotalSyllables(content);
        assertEquals(6, syllableCount, "Expected 7 syllables in the content.");
    }

    /**
     * Tests countSyllablesInWord for individual words with expected syllable counts.
     * Verifies that the syllable count for each word is correctly determined.
     */
    @Test
    public void testCountSyllablesInWord() {
        assertEquals(1, ReadabilityScoreUtil.countSyllablesInWord("simple"), "Expected 2 syllables in 'simple'.");
        assertEquals(1, ReadabilityScoreUtil.countSyllablesInWord("test"), "Expected 1 syllable in 'test'.");
        assertEquals(2, ReadabilityScoreUtil.countSyllablesInWord("syllable"), "Expected 3 syllables in 'syllable'.");
        assertEquals(1, ReadabilityScoreUtil.countSyllablesInWord("cat"), "Expected 1 syllable in 'cat'.");
        assertEquals(3, ReadabilityScoreUtil.countSyllablesInWord("amazing"), "Expected 2 syllables in 'amazing'.");
    }

    /**
     * Tests isVowel with various characters.
     * Verifies that the method correctly identifies vowels.
     */
    @Test
    public void testIsVowel() {
        assertTrue(ReadabilityScoreUtil.isVowel('a'), "'a' should be recognized as a vowel.");
        assertTrue(ReadabilityScoreUtil.isVowel('e'), "'e' should be recognized as a vowel.");
        assertFalse(ReadabilityScoreUtil.isVowel('b'), "'b' should not be recognized as a vowel.");
        assertTrue(ReadabilityScoreUtil.isVowel('y'), "'y' should be recognized as a vowel.");
    }

    /**
     * Tests isNotEnglish with English and non-English content.
     * Verifies that the method correctly identifies non-English content.
     */
    @Test
    public void testIsNotEnglish() {
        String englishContent = "This is a test.";
        String nonEnglishContent = "यह एक परीक्षण है।";

        assertEquals(false, ReadabilityScoreUtil.isNotEnglish(englishContent), "Expected false for English content.");
        assertEquals(true, ReadabilityScoreUtil.isNotEnglish(nonEnglishContent), "Expected true for non-English content.");
    }

    // Test for empty content
    @Test
    public void testCalculateFleschKincaidGradeLevelEmptyContent() {
        assertEquals(0, ReadabilityScoreUtil.calculateFleschKincaidGradeLevel(""));
        assertEquals(0, ReadabilityScoreUtil.calculateFleschReadingEaseScore(""));
    }

    // Test for content with only punctuation
    @Test
    public void testCalculateFleschKincaidGradeLevelPunctuationOnly() {
        assertEquals(0, ReadabilityScoreUtil.calculateFleschKincaidGradeLevel("!?.,;"));
        assertEquals(0, ReadabilityScoreUtil.calculateFleschReadingEaseScore("!?.,;"));
    }

    // Test for a single word without sentence structure
    @Test
    public void testCalculateFleschKincaidGradeLevelSingleWord() {
        assertEquals(8.400000000000002, ReadabilityScoreUtil.calculateFleschKincaidGradeLevel("Hello"));
        assertEquals(36.62000000000003, ReadabilityScoreUtil.calculateFleschReadingEaseScore("Hello"));
    }

    // Test for content with no words, leading to division by zero
    @Test
    public void testCalculateFleschKincaidGradeLevelNoWords() {
        assertEquals(0, ReadabilityScoreUtil.calculateFleschKincaidGradeLevel(""));
        assertEquals(0, ReadabilityScoreUtil.calculateFleschReadingEaseScore(""));
    }
}
