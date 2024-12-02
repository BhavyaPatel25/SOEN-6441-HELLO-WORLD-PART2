package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Actor responsible for calculating word-level statistics for a list of descriptions.
 *
 * <p>The {@code WordStatActor} processes a list of video descriptions, counts the occurrences
 * of words, and sorts them by frequency. It sends the result as a {@link WordStatResponse} back to
 * the requesting actor.</p>
 *
 * <p>Messages handled by this actor include {@link WordStatRequest} for requesting word statistics
 * and {@link WordStatResponse} for providing results.</p>
 */
public class WordStatActor extends AbstractActor {

    private final ActorRef userActor;

    /**
     * Constructs a {@code WordStatActor} instance.
     *
     * @param userActor the actor to send word statistics results to
     */
    public WordStatActor(ActorRef userActor) {
        this.userActor = userActor;
    }

    /**
     * Factory method to create {@link Props} for the {@code WordStatActor}.
     *
     * @param userActor the actor to send results back to
     * @return a {@link Props} instance for creating {@code WordStatActor} instances
     */
    public static Props props(ActorRef userActor) {
        return Props.create(WordStatActor.class, () -> new WordStatActor(userActor));
    }

    /**
     * Defines the receive behavior for the {@code WordStatActor}.
     *
     * <p>This method listens for {@link WordStatRequest} messages, processes the descriptions
     * to calculate word-level statistics, and sends a {@link WordStatResponse} back to the
     * {@code userActor}.</p>
     *
     * @return the receive behavior for handling messages
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WordStatRequest.class, this::handleWordStatRequest)
                .build();
    }

    /**
     * Handles the {@link WordStatRequest} message to perform word statistics.
     *
     * <p>Performs word count analysis on the provided descriptions and sorts the words by frequency.</p>
     *
     * @param request the word statistics request containing the descriptions to analyze
     */
    private void handleWordStatRequest(WordStatRequest request) {
        System.out.println("WordStatActor received request for query: " + request.query);

        if (request.descriptions == null || request.descriptions.isEmpty()) {
            System.err.println("No descriptions provided for word statistics.");
            userActor.tell(new WordStatResponse(request.query, Collections.emptyMap()), self());
            return;
        }

        try {
            // Perform word-level statistics on descriptions
            Map<String, Long> wordStats = request.descriptions.stream()
                    .flatMap(description -> Arrays.stream(description.toLowerCase().split("\\W+")))  // Split into words
                    .filter(word -> !word.isEmpty())  // Ignore empty words
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));  // Count occurrences

            // Sort words by frequency in descending order
            Map<String, Long> sortedWordStats = wordStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            // Send the sorted word statistics response to the UserActor
            userActor.tell(new WordStatResponse(request.query, sortedWordStats), self());
        } catch (Exception e) {
            System.err.println("Error during word statistics calculation: " + e.getMessage());
            e.printStackTrace();
            userActor.tell(new WordStatResponse(request.query, Collections.emptyMap()), self());
        }
    }

    /**
     * Message class representing a request for word-level statistics.
     *
     * <p>The {@code WordStatRequest} contains the search query and a list of video descriptions to analyze.</p>
     *
     * <p>Instances of this class are immutable and are used to pass data between actors.</p>
     */
    public static class WordStatRequest {
        /**
         * The search query string associated with the request.
         */
        public final String query;

        /**
         * The list of video descriptions to analyze.
         */
        public final List<String> descriptions;

        /**
         * Constructs a {@code WordStatRequest} instance.
         *
         * @param query        the search query string
         * @param descriptions the list of video descriptions to analyze
         */
        public WordStatRequest(String query, List<String> descriptions) {
            this.query = query;
            this.descriptions = descriptions;
        }
    }

    /**
     * Message class representing the results of word-level statistics.
     *
     * <p>The {@code WordStatResponse} contains the search query and the sorted word statistics.</p>
     *
     * <p>Instances of this class are immutable and are used to pass data between actors.</p>
     */
    public static class WordStatResponse {
        /**
         * The search query associated with the word statistics result.
         */
        final String query;

        /**
         * The sorted word statistics as a map, with words as keys and their frequencies as values.
         */
        final Map<String, Long> wordStats;

        /**
         * Constructs a {@code WordStatResponse} instance.
         *
         * @param query     the search query
         * @param wordStats the sorted word statistics
         */
        public WordStatResponse(String query, Map<String, Long> wordStats) {
            this.query = query;
            this.wordStats = wordStats;
        }

        /**
         * Provides a string representation of the word statistics response.
         *
         * @return a string representing the {@code WordStatResponse}
         */
        @Override
        public String toString() {
            return "WordStatResponse{" +
                    "query='" + query + '\'' +
                    ", wordStats=" + wordStats +
                    '}';
        }
    }
}
