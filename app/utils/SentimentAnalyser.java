package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for analyzing the sentiment of text descriptions.
 *
 * <p>This class provides methods to determine if a description or a list of descriptions
 * is overall happy, sad, or neutral based on the occurrence of predefined keywords.</p>
 *
 * <p>Sentiment is determined by a 70% threshold rule for the counts of happy and sad keywords.</p>
 *
 * <p>Keyword lists are loaded from external files located in the {@code conf} directory.</p>
 */
public class SentimentAnalyser {

    private  final List<String> HAPPY_KEYWORDS;
    private  final List<String> SAD_KEYWORDS;

    // Constructor for testing purposes
    public SentimentAnalyser(List<String> happyKeywords, List<String> sadKeywords) {
        this.HAPPY_KEYWORDS = happyKeywords;
        this.SAD_KEYWORDS = sadKeywords;
    }
    // Default constructor for production usage
    public SentimentAnalyser() {
        this.HAPPY_KEYWORDS = loadKeywordsFromFile("conf/happy_keywords");
        this.SAD_KEYWORDS = loadKeywordsFromFile("conf/sad_keywords");
    }

    /**
     * Loads keywords from a specified text file.
     *
     * <p>Each line in the file is treated as a separate keyword.</p>
     *
     * @param filePath the path to the text file containing keywords, one per line
     * @return a list of keywords loaded from the file, or an empty list if an error occurs
     */
    protected static List<String> loadKeywordsFromFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading keywords from " + filePath + ", returning empty list.");
            return Collections.emptyList();
        }
    }

    /**
     * Analyzes the sentiment of a single description.
     *
     * <p>This method counts all occurrences of happy and sad keywords in the description
     * and determines the sentiment based on a 70% threshold rule.</p>
     *
     * @param description the text description to analyze
     * @return "happy" if the sentiment is predominantly positive, "sad" if predominantly negative,
     *         or "neutral" if neither is predominant
     */
    public String analyzeDescription(String description) {
        if (description == null || description.isEmpty()) {
            return ":-|";
        }

        String lowerCaseDescription = description.toLowerCase();

        // Count total occurrences of happy and sad keywords
        long happyCount = HAPPY_KEYWORDS.stream()
                .mapToLong(keyword -> countOccurrences(lowerCaseDescription, keyword))
                .sum();

        long sadCount = SAD_KEYWORDS.stream()
                .mapToLong(keyword -> countOccurrences(lowerCaseDescription, keyword))
                .sum();

        // Determine sentiment based on the 70% threshold rule
        long totalCount = happyCount + sadCount;
        if (totalCount > 0) {
            double happyPercentage = (double) happyCount / totalCount;
            if (happyPercentage > 0.7) {
                return ":-)";
            } else if (happyPercentage < 0.3) {
                return ":-(";
            }
        }

        return ":-|";
    }

    /**
     * Analyzes the overall sentiment of a list of descriptions.
     *
     * <p>This method aggregates individual sentiments from each description and determines
     * the overall sentiment based on a 70% threshold rule.</p>
     *
     * @param descriptions a list of text descriptions to analyze
     * @return "happy" if the overall sentiment is predominantly positive, "sad" if predominantly negative,
     *         or "neutral" if neither is predominant
     */
    public String analyzeOverallSentiment(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return ":-|"; // Default sentiment when no descriptions are provided
        }

        long happyCount = 0;
        long sadCount = 0;

        // Analyze each description individually and tally results
        for (String description : descriptions) {
            String sentiment = analyzeDescription(description);
            if ("happy".equals(sentiment)) {
                happyCount++;
            } else if ("sad".equals(sentiment)) {
                sadCount++;
            }
        }

        // Calculate the overall sentiment based on tallies
        long total = happyCount + sadCount;
        if (total > 0) {
            double happyPercentage = (double) happyCount / total;
            if (happyPercentage > 0.7) {
                return ":-)";
            } else if (happyPercentage < 0.3) {
                return ":-(";
            }
        }

        return ":-|"; // Default to neutral when no strong sentiment is present
    }

    /**
     * Counts the occurrences of a specific keyword in a description.
     *
     * @param text    the text to search within
     * @param keyword the keyword to search for
     * @return the number of times the keyword appears in the text
     */
    private long countOccurrences(String text, String keyword) {
        int index = 0;
        long count = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length(); // Move past the current match
        }
        return count;
    }
}
