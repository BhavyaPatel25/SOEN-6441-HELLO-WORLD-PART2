package utils;

/**
 * Utility class for calculating readability scores for text content.
 * This class provides methods to calculate various readability scores
 * based on the content of a text, with a primary focus on the Flesch-Kincaid
 * Grade Level. The Flesch-Kincaid Grade Level is a readability test designed
 * to indicate the complexity of English text based on sentence length, word
 * length, and syllable count.
 *
 * <p>
 * This class includes the following functionality:
 * <ul>
 *   <li>Calculating the Flesch-Kincaid Grade Level for a given text.</li>
 *   <li>Supporting methods for counting words, sentences, and syllables.</li>
 *   <li>Handling non-English or empty content by returning a default score.</li>
 * </ul>
 *
 *
 * <p>
 * Example usage:
 * <pre>
 *     String content = "This is a sample text.";
 *     double gradeLevel = ReadabilityScoreUtil.calculateFleschKincaidGradeLevel(content);
 *     System.out.println("Grade Level: " + gradeLevel);
 * </pre>
 *
 *
 * @see <a href="https://en.wikipedia.org/wiki/Flesch%E2%80%93Kincaid_readability_tests">Flesch-Kincaid Readability Tests</a>
 */
public class ReadabilityScoreUtil {

    /**
     * Calculates the Flesch-Kincaid Grade Level for a given text content.
     * The Flesch-Kincaid Grade Level is a readability test designed to indicate
     * the complexity of English text based on the number of words, sentences,
     * and syllables in the content.
     * <p>
     * The formula used is:
     * <pre>
     *   (0.39 * (words / sentences)) + (11.8 * (syllables / words)) - 15.59
     * </pre>
     * This method returns a grade level which represents the years of education
     * required to understand the content.
     * <p>
     * If the content is not in English or is empty, the method returns 0.
     *
     * @param content The text content for which the Flesch-Kincaid Grade Level is calculated.
     * @return The Flesch-Kincaid Grade Level score for the content. Returns 0 if the content is not in English or is empty.
     */
    public static double calculateFleschKincaidGradeLevel(String content) {
        if (isNotEnglish(content) || content.isEmpty()) {
            return 0;
        }
        int sentences = countSentences(content);
        int words = countWords(content);
        int syllables = countTotalSyllables(content);  // Use renamed method


        double score = (0.39 * ((double) words / sentences)) + (11.8 * ((double) syllables / words)) - 15.59;
        return Double.isNaN(score) ? 0 : score;
    }

    /**
     * Calculates the Flesch Reading Ease score for a given text content.
     * The Flesch Reading Ease score is a readability test designed to indicate
     * how easy or difficult it is to understand a piece of English text.
     * <p>
     * The formula used is:
     * <pre>
     *   206.835 - (1.015 * (words / sentences)) - (84.6 * (syllables / words))
     * </pre>
     * This method returns a score that ranges from 0 to 100. Higher scores
     * indicate easier-to-read text, and lower scores indicate more difficult text.
     * <p>
     * If the content is not in English or is empty, the method returns 0.
     *
     * @param content The text content for which the Flesch Reading Ease score is calculated.
     * @return The Flesch Reading Ease score for the content. Returns 0 if the content is not in English or is empty.
     *
     */
    public static double calculateFleschReadingEaseScore(String content) {
        if (isNotEnglish(content) || content.isEmpty()) {
            return 0;
        }
        int sentences = countSentences(content);
        int words = countWords(content);
        int syllables = countTotalSyllables(content);  // Use renamed method

        double score = 206.835 - (1.015 * ((double) words / sentences)) - (84.6 * ((double) syllables / words));
        return Double.isNaN(score) ? 0 : score;
    }

    /**
     * Counts the number of sentences in a given text content.
     * A sentence is considered to end with one of the following punctuation marks: period (.),
     * exclamation mark (!), or question mark (?).
     * <p>
     * This method splits the content by the sentence-ending punctuation marks and counts the resulting segments.
     * Note that this method does not handle edge cases like abbreviations, decimal numbers, or other sentence delimiters.
     *
     * @param content The text content in which sentences are to be counted.
     * @return The number of sentences in the content.
     */
    public static int countSentences(String content) {
        return content.split("[.!?]").length;
    }

    /**
     * Counts the number of words in the given text content.
     * The method first removes any non-alphanumeric characters (except spaces) from the content,
     * then splits the content by whitespace to count the words.
     * <p>
     * Words are defined as sequences of alphanumeric characters separated by spaces.
     * Punctuation and other non-alphanumeric characters are ignored in the word count.
     *
     * @param content The text content in which words are to be counted.
     * @return The number of words in the content.
     */
    public static int countWords(String content) {
        String cleanedContent = content.replaceAll("[^a-zA-Z0-9\\s]", " ");
        return cleanedContent.split("\\s+").length;
    }

    /**
     * Counts the total number of syllables in the given text content.
     * The method first removes punctuation and converts the content to lowercase,
     * then splits the content into individual words to count the syllables in each word.
     * <p>
     * It uses the helper method {@link #countSyllablesInWord(String)} to count syllables in each word.
     *
     * @param s The text content in which syllables are to be counted.
     * @return The total number of syllables in the content.
     */
    public static int countTotalSyllables(String s) {
        // Clean the content by removing punctuation
        String cleanedContent = s.replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase();
        int count = 0;

        // Split into words for syllable counting
        String[] words = cleanedContent.split("\\s+");
        for (String word : words) {
            count += countSyllablesInWord(word);
        }
        return count;
    }

    /**
     * Counts the number of syllables in a single word.
     * The method treats consecutive vowels as one syllable and applies special rules for silent 'e' at the end of words.
     * <p>
     * It processes the word by first converting it to lowercase and removing non-letter characters.
     * It then counts syllables based on the presence of vowels, ensuring that silent 'e' at the end of words is considered.
     *
     * @param word The word for which syllables need to be counted.
     * @return The number of syllables in the word. If no syllables are detected, at least 1 syllable is returned.
     */
    public static int countSyllablesInWord(String word) {
        word = word.toLowerCase().replaceAll("[^a-zA-Z]", " "); // Remove non-letter characters
        if (word.length() == 0) return 0; // Handle empty words

        boolean lastWasVowel = false;
        int count = 0;

        for (int i = 0; i < word.length(); i++) {
            char wc = word.charAt(i);
            if (isVowel(wc)) {
                if (!lastWasVowel) {
                    count++;
                }
                lastWasVowel = true;
            } else {
                lastWasVowel = false;
            }
        }

        // If the word ends with a silent 'e', decrease syllable count by 1, but only if count is greater than 1
        // Ensure we only apply this rule if the syllable count is reasonable (e.g., greater than 1)
        if (word.endsWith("e") && count > 1) {
            count--;
        }

        // Make sure to return at least 1 syllable, as most words have at least one syllable
        return Math.max(count, 1);
    }

    /**
     * Checks if the given character is a vowel.
     * This method checks if the character belongs to the set of vowels ('a', 'e', 'i', 'o', 'u', 'y').
     *
     * @param c The character to check.
     * @return {@code true} if the character is a vowel, {@code false} otherwise.
     */
    public static boolean isVowel(char c) {
        return "aeiouy".indexOf(c) != -1;
    }

    /**
     * Checks if the given content is written in a non-English script (specifically in Hindi).
     * This method uses a regular expression to detect the presence of characters in the Unicode range for Hindi.
     *
     * @param content The content to check.
     * @return {@code true} if the content contains non-English (Hindi) characters, {@code false} otherwise.
     */
    public static boolean isNotEnglish(String content) {
        return content.matches(".*[\\u0900-\\u097F]+.*");
    }
}
