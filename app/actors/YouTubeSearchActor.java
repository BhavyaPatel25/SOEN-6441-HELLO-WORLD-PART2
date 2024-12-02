package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.libs.Json;
import services.YouTubeService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Actor responsible for handling YouTube video searches.
 *
 * <p>The {@code YouTubeSearchActor} performs asynchronous video searches using the {@link YouTubeService},
 * formats the results into JSON, and sends them to the requesting {@code UserActor}. It also triggers
 * sentiment analysis on video descriptions if applicable.</p>
 */
public class YouTubeSearchActor extends AbstractActor {

    private final String query;
    private final YouTubeService youtubeService;
    private final ActorRef userActor;

    /**
     * Constructs a {@code YouTubeSearchActor} instance.
     *
     * @param query         the search query string
     * @param youtubeService the service used to interact with the YouTube API
     * @param userActor     the actor to send the search results and sentiment analysis requests
     */
    public YouTubeSearchActor(String query, YouTubeService youtubeService, ActorRef userActor) {
        this.query = query;
        this.youtubeService = youtubeService;
        this.userActor = userActor;
    }

    /**
     * Creates a {@link Props} instance for {@code YouTubeSearchActor}.
     *
     * <p>This factory method is used to instantiate the actor with the required dependencies.</p>
     *
     * @param query         the search query string
     * @param youtubeService the service for interacting with the YouTube API
     * @param userActor     the actor to send the results and requests
     * @return a {@link Props} instance for creating {@code YouTubeSearchActor} instances
     */
    public static Props props(String query, YouTubeService youtubeService, ActorRef userActor) {
        return Props.create(YouTubeSearchActor.class, query, youtubeService, userActor);
    }

    /**
     * Defines the receive behavior of the actor.
     *
     * <p>The actor listens for the "fetch" message, initiates a YouTube search asynchronously,
     * formats the results into JSON, and sends them to the {@code UserActor}.
     * If no results are found, an empty JSON array is sent to the {@code UserActor}.</p>
     *
     * @return the receive behavior of the actor, defining how it handles incoming messages
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("fetch", msg -> {
                    youtubeService.searchVideosAsync(query)
                            .thenAccept(results -> {
                                if (!results.isEmpty()) {
                                    // Extract descriptions for sentiment analysis
                                    List<String> descriptions = results.stream()
                                            .map(result -> result.get("description"))
                                            .filter(description -> description != null && !description.isEmpty())
                                            .collect(Collectors.toList());

                                    // Convert results to JSON and send to UserActor
                                    JsonNode jsonResults = formatResultsAsJson(results);
                                    userActor.tell(jsonResults, self());

                                    // Send descriptions for sentiment analysis
                                    userActor.tell(new SentimentActor.SentimentRequest(query, descriptions), self());
                                } else {
                                    // Send empty results
                                    ArrayNode emptyResults = Json.newArray();
                                    userActor.tell(emptyResults, self());
                                }
                            })
                            .exceptionally(e -> {
                                e.printStackTrace();
                                return null;
                            });
                })
                .build();
    }

    /**
     * Formats the search results into JSON.
     *
     * <p>Converts a list of maps (representing video metadata) into a JSON array,
     * which is easier to serialize and send to the {@code UserActor}.</p>
     *
     * @param results the list of video metadata maps
     * @return a JSON array representing the search results in a structured format
     */
    private JsonNode formatResultsAsJson(List<Map<String, String>> results) {
        return Json.toJson(results);
    }

    /**
     * Lifecycle hook invoked before the actor starts.
     *
     * <p>Triggers an initial "fetch" operation immediately when the actor starts,
     * ensuring that the search results are fetched as soon as the actor is created.</p>
     */
    @Override
    public void preStart() {
        self().tell("fetch", self());
    }

    /**
     * Lifecycle hook invoked after the actor stops.
     *
     * <p>Performs cleanup tasks or logs messages to indicate the actor's termination.</p>
     */
    @Override
    public void postStop() {
        System.out.println("YouTubeSearchActor stopped for query: " + query);
    }
}