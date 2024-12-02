package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import utils.ReadabilityScoreUtil;
import utils.ReadabilityScores;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Actor responsible for analyzing the readability of video descriptions for a given query.
 *
 * <p>The {@code ReadabilityScoreActor} uses the {@link ReadabilityScoreUtil} utility
 * to compute readability scores, such as Flesch-Kincaid Grade Level and Flesch Reading Ease Score,
 * for a list of descriptions. Results are sent back to the requesting {@link UserActor}.</p>
 *
 * <p>Messages handled by this actor include {@link ScoreRequest} and result responses
 * as {@link ScoreResponse}.</p>
 *
 * @author Khushi
 */
public class ReadabilityScoreActor extends AbstractActor {

    private final ReadabilityScoreUtil readabilityScoreUtil;
    private final ActorRef userActor;

    /**
     * Constructs a {@code ReadabilityScoreActor} instance.
     *
     * @param readabilityScoreUtil the utility class used for readability analysis
     * @param userActor            the actor to send readability score results to
     */
    public ReadabilityScoreActor(ReadabilityScoreUtil readabilityScoreUtil, ActorRef userActor) {
        this.readabilityScoreUtil = readabilityScoreUtil;
        this.userActor = userActor;
    }

    /**
     * Factory method to create {@link Props} for the {@code ReadabilityScoreActor}.
     *
     * @param readabilityScoreUtil the {@link ReadabilityScoreUtil} instance to be used
     * @param userActor            the actor to send results back to
     * @return a {@link Props} instance for creating {@code ReadabilityScoreActor} instances
     */
    public static Props props(ReadabilityScoreUtil readabilityScoreUtil, ActorRef userActor) {
        return Props.create(ReadabilityScoreActor.class, () -> new ReadabilityScoreActor(readabilityScoreUtil, userActor));
    }

    /**
     * Defines the receive behavior for the {@code ReadabilityScoreActor}.
     *
     * <p>This method listens for {@link ScoreRequest} messages, performs readability analysis
     * using the {@link ReadabilityScoreUtil}, and sends a {@link ScoreResponse}
     * back to the {@code UserActor}.</p>
     *
     * @return the receive behavior for handling messages
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ScoreRequest.class, this::handleScoreRequest)
                .build();
    }

    /**
     * Handles the {@link ScoreRequest} message to perform readability analysis.
     *
     * <p>Performs analysis on the provided descriptions, computes individual scores,
     * and sends the results back as a {@link ScoreResponse}.</p>
     *
     * @param request the readability analysis request containing the query and descriptions
     */
    private void handleScoreRequest(ScoreRequest request) {
        System.out.println("ReadabilityScoreActor received request for query: " + request.query);

        if (request.descriptions == null || request.descriptions.isEmpty()) {
            System.err.println("No descriptions provided for readability analysis.");
            userActor.tell(
                    new ScoreResponse(request.query, List.of(), 0.0, 0.0),
                    self()
            );
            return;
        }

        try {
            // Perform readability analysis on descriptions
            List<ReadabilityScores> scores = request.descriptions.stream()
                    .map(description -> new ReadabilityScores(
                            ReadabilityScoreUtil.calculateFleschKincaidGradeLevel(description),
                            ReadabilityScoreUtil.calculateFleschReadingEaseScore(description)
                    ))
                    .collect(Collectors.toList());

            // Compute overall scores (e.g., averages)
            double overallGradeLevel = scores.stream()
                    .mapToDouble(ReadabilityScores::getGradeLevel)
                    .average()
                    .orElse(0.0);

            double overallEaseScore = scores.stream()
                    .mapToDouble(ReadabilityScores::getReadingEase)
                    .average()
                    .orElse(0.0);

            // Send response to UserActor
            ScoreResponse response = new ScoreResponse(request.query, scores, overallGradeLevel, overallEaseScore);
            userActor.tell(response, self());
        } catch (Exception e) {
            System.err.println("Error during readability analysis: " + e.getMessage());
            e.printStackTrace();
            userActor.tell(
                    new ScoreResponse(request.query, List.of(), 0.0, 0.0),
                    self()
            );
        }
    }

    /**
     * Message class representing a request for readability analysis.
     *
     * <p>The {@code ScoreRequest} contains the search query and a list of
     * descriptions to analyze.</p>
     *
     * <p>Instances of this class are immutable and are used to pass data between actors.</p>
     */
    public static class ScoreRequest {
        /**
         * The search query string.
         */
        public final String query;

        /**
         * The list of descriptions to analyze.
         */
        public final List<String> descriptions;

        /**
         * Constructs a {@code ScoreRequest} instance.
         *
         * @param query        the search query string
         * @param descriptions the list of descriptions to analyze
         */
        public ScoreRequest(String query, List<String> descriptions) {
            this.query = query;
            this.descriptions = descriptions;
        }
    }

    /**
     * Message class representing the results of readability analysis.
     *
     * <p>The {@code ScoreResponse} contains the search query, individual readability scores
     * for each description, and the overall scores for the query.</p>
     *
     * <p>Instances of this class are immutable and are used to pass data between actors.</p>
     */
    public static class ScoreResponse {
        /**
         * The search query string.
         */
        final String query;

        /**
         * The list of individual readability scores.
         */
        final List<ReadabilityScores> scores;

        /**
         * The overall Flesch-Kincaid Grade Level score for the descriptions.
         */
        private final double overallGradeLevel;

        /**
         * The overall Flesch Reading Ease score for the descriptions.
         */
        private final double overallEaseScore;

        /**
         * Constructs a {@code ScoreResponse} instance.
         *
         * @param query            the search query string
         * @param scores           the list of individual readability scores
         * @param overallGradeLevel the overall grade level score
         * @param overallEaseScore  the overall reading ease score
         */
        public ScoreResponse(String query, List<ReadabilityScores> scores, double overallGradeLevel, double overallEaseScore) {
            this.query = query;
            this.scores = scores;
            this.overallGradeLevel = overallGradeLevel;
            this.overallEaseScore = overallEaseScore;
        }

        public double getOverallGradeLevel() {
            return overallGradeLevel;
        }

        public double getOverallEaseScore() {
            return overallEaseScore;
        }

        @Override
        public String toString() {
            return "ScoreResponse{" +
                    "query='" + query + '\'' +
                    ", scores=" + scores +
                    ", overallGradeLevel=" + overallGradeLevel +
                    ", overallEaseScore=" + overallEaseScore +
                    '}';
        }
    }
}
