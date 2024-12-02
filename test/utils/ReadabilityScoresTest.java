package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReadabilityScoresTest {

    @Test
    public void testConstructorAndGetters() {
        // Arrange
        double expectedGradeLevel = 8.5;
        double expectedReadingEase = 75.2;

        // Act
        ReadabilityScores scores = new ReadabilityScores(expectedGradeLevel, expectedReadingEase);

        // Assert
        assertEquals(expectedGradeLevel, scores.getGradeLevel(), "Grade level should match the value passed in the constructor.");
        assertEquals(expectedReadingEase, scores.getReadingEase(), "Reading ease should match the value passed in the constructor.");
    }

    @Test
    public void testToString() {
        // Arrange
        double gradeLevel = 8.5;
        double readingEase = 75.2;
        ReadabilityScores scores = new ReadabilityScores(gradeLevel, readingEase);
        String expectedString = "ReadabilityScores{gradeLevel=8.5, readingEase=75.2}";

        // Act
        String actualString = scores.toString();

        // Assert
        assertEquals(expectedString, actualString, "toString should return the correct string representation.");
    }
}
