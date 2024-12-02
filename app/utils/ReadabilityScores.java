package utils;

public class ReadabilityScores {
    private final double gradeLevel;
    private final double readingEase;

    public ReadabilityScores(double gradeLevel, double readingEase) {
        this.gradeLevel = gradeLevel;
        this.readingEase = readingEase;
    }

    public double getGradeLevel() {
        return gradeLevel;
    }

    public double getReadingEase() {
        return readingEase;
    }

    @Override
    public String toString() {
        return "ReadabilityScores{" +
                "gradeLevel=" + gradeLevel +
                ", readingEase=" + readingEase +
                '}';
    }
}
