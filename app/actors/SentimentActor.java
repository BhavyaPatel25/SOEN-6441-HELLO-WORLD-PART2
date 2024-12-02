package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import utils.SentimentAnalyser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Actor responsible for analyzing the sentiment of video descriptions for a given query.
 *
 * <p>The {@code SentimentActor} uses the {@link SentimentAnalyser} utility to perform
 * sentiment analysis on individual descriptions and compute the overall sentiment
 * for the query. Results are sent back to the requesting {@link UserActor}.</p>
 *
 * <p>Messages handled by this actor include {@link SentimentRequest} and result responses
 * as {@link SentimentResponse}.</p>
 */
public class SentimentActor extends AbstractActor {

    private final SentimentAnalyser sentimentAnalyser;
    private final ActorRef userActor;

    /**
     * Constructs a {@code SentimentActor} instance.
     *
     * @param sentimentAnalyser the utility class used for sentiment analysis
     * @param userActor         the actor to send sentiment analysis results to
     */
    public SentimentActor(SentimentAnalyser sentimentAnalyser, ActorRef userActor) {
        this.sentimentAnalyser = sentimentAnalyser;
        this.userActor = userActor;
    }

    /**
     * Factory method to create {@link Props} for the {@code SentimentActor}.
     *
     * @param sentimentAnalyser the {@link SentimentAnalyser} instance to be used
     * @param userActor         the actor to send results back to
     * @return a {@link Props} instance for creating {@code SentimentActor} instances
     */
    public static Props props(SentimentAnalyser sentimentAnalyser, ActorRef userActor) {
        return Props.create(SentimentActor.class, () -> new SentimentActor(sentimentAnalyser, userActor));
    }

    /**
     * Defines the receive behavior for the {@code SentimentActor}.
     *
     * <p>This method listens for {@link SentimentRequest} messages, performs sentiment
     * analysis using the {@link SentimentAnalyser}, and sends a {@link SentimentResponse}
     * back to the {@code UserActor}.</p>
     *
     * @return the receive behavior for handling messages
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SentimentRequest.class, this::handleSentimentRequest)
                .build();
    }

    /**
     * Handles the {@link SentimentRequest} message to perform sentiment analysis.
     *
     * <p>Performs analysis on the provided descriptions, computes the overall sentiment,
     * and sends the results back as a {@link SentimentResponse}.</p>
     *
     * @param request the sentiment analysis request containing the query and descriptions
     */
    private void handleSentimentRequest(SentimentRequest request) {
        System.out.println("SentimentActor received request for query: " + request.query);

        if (request.descriptions == null || request.descriptions.isEmpty()) {
            System.err.println("No descriptions provided for sentiment analysis.");
            userActor.tell(
                    new SentimentResponse(request.query, List.of(), "neutral"),
                    self()
            );
            return;
        }

        try {
            // Perform sentiment analysis on descriptions
            List<String> sentiments = request.descriptions.stream()
                    .map(sentimentAnalyser::analyzeDescription)
                    .collect(Collectors.toList());

            String overallSentiment = sentimentAnalyser.analyzeOverallSentiment(request.descriptions);

            // Create a response object
            SentimentResponse response = new SentimentResponse(request.query, sentiments, overallSentiment);

            // Send response to UserActor
            userActor.tell(response, self());
        } catch (Exception e) {
            System.err.println("Error during sentiment analysis: " + e.getMessage());
            e.printStackTrace();
            userActor.tell(
                    new SentimentResponse(request.query, List.of(), "neutral"),
                    self()
            );
        }
    }

    /**
     * Message class representing a request for sentiment analysis.
     *
     * <p>The {@code SentimentRequest} contains the search query and a list of
     * video descriptions to analyze.</p>
     *
     * <p>Instances of this class are immutable and are used to pass data between actors.</p>
     */
    public static class SentimentRequest {
        public final String query;
        public final List<String> descriptions;

        /**
         * Constructs a {@code SentimentRequest} instance.
         *
         * @param query        the search query string
         * @param descriptions the list of video descriptions to analyze
         */
        public SentimentRequest(String query, List<String> descriptions) {
            this.query = query;
            this.descriptions = descriptions;
        }
    }

    /**
     * Message class representing the results of sentiment analysis.
     *
     * <p>The {@code SentimentResponse} contains the search query, individual sentiments
     * for each description, and the overall sentiment for the query.</p>
     *
     * <p>Instances of this class are immutable and are used to pass data between actors.</p>
     */
    public static class SentimentResponse {
        public final String query;
        public final List<String> sentiments;
        public final String overallSentiment;

        /**
         * Constructs a {@code SentimentResponse} instance.
         *
         * @param query           the search query string
         * @param sentiments      the list of sentiments for individual descriptions
         * @param overallSentiment the overall sentiment for the query
         */
        public SentimentResponse(String query, List<String> sentiments, String overallSentiment) {
            this.query = query;
            this.sentiments = sentiments;
            this.overallSentiment = overallSentiment;
        }
    }
}
