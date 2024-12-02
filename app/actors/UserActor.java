package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import services.YouTubeService;
import utils.ReadabilityScoreUtil;
import utils.ReadabilityScores;
import utils.SentimentAnalyser;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Actor responsible for managing WebSocket interactions, YouTube searches,
 * sentiment analysis, tag-related requests, and channel profile requests.
 *
 * <p>The {@code UserActor} processes various WebSocket commands, such as searching for videos, fetching
 * video details, retrieving channel profiles, and analyzing the sentiment and readability of descriptions.</p>
 */
public class UserActor extends AbstractActor {

    private final ActorRef webSocket;
    private final YouTubeService youTubeService;
    private final Map<String, ActorRef> youtubeSearchActors = new HashMap<>();

    /**
     * Constructs a {@code UserActor} instance.
     *
     * @param webSocket      the WebSocket connection associated with this actor
     * @param youTubeService the service used for interacting with the YouTube API
     */
    public UserActor(ActorRef webSocket, YouTubeService youTubeService) {
        this.webSocket = webSocket;
        this.youTubeService = youTubeService;
    }

    /**
     * Creates a {@link Props} instance for the {@code UserActor}.
     *
     * @param ws             the WebSocket connection
     * @param youTubeService the service for interacting with the YouTube API
     * @return a {@link Props} instance for creating {@code UserActor} instances
     */
    public static Props props(final ActorRef ws, YouTubeService youTubeService) {
        return Props.create(UserActor.class, ws, youTubeService);
    }

    /**
     * Defines the actor's behavior by specifying how it reacts to incoming messages.
     *
     * @return a {@link Receive} object defining the message handling logic
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, this::handleWebSocketCommand)
                .match(SearchWrapper.class, this::handleSearchResults)
                .match(SentimentActor.SentimentResponse.class, this::handleSentimentResults)
                .match(ChannelProfileActor.ChannelProfileResponse.class, this::handleChannelProfileResults)
                .match(ReadabilityScoreActor.ScoreResponse.class, this::handleScoreResults)
                .match(Terminated.class, this::handleActorTermination)
                .match(TagDetailsRequest.class, this::handleTagDetails)
                .build();
    }

    /**
     * Handles incoming WebSocket commands.
     *
     * @param message the incoming WebSocket message in JSON format
     */
    private void handleWebSocketCommand(JsonNode message) {
        if (message.isObject() && message.has("action")) {
            String action = message.get("action").asText();

            switch (action) {
                case "search":
                    handleSearchCommand(message);
                    break;
                case "getVideoDetails":
                    String videoId = message.get("videoId").asText();
                    ActorRef videoDetailsActor = getContext().actorOf(VideoDetailsActor.props(youTubeService));
                    videoDetailsActor.tell(new VideoDetailsActor.FetchVideoDetails(videoId), self());
                    break;
                case "getChannelProfile":
                    String channelId = message.get("channelId").asText();
                    handleChannelProfileCommand(channelId);
                    break;
                case "getTagDetails":
                    String tag = message.get("tag").asText();
                    self().tell(new TagDetailsRequest(tag), self());
                    break;
                default:
                    System.err.println("Unknown WebSocket action: " + action);
            }
        } else {
            System.err.println("Invalid WebSocket command: " + message.toString());
        }
    }

    /**
     * Handles the "search" WebSocket command to fetch results.
     *
     * @param message the WebSocket message containing the search query
     */
    private void handleSearchCommand(JsonNode message) {
        String query = message.get("query").asText();
        CompletableFuture<List<Map<String, String>>> futureResults = youTubeService.searchVideosAsync(query);
        self().tell(new SearchWrapper(query, futureResults), self());
    }

    /**
     * Handles the "getChannelProfile" WebSocket command.
     *
     * @param channelId the ID of the channel to fetch the profile for
     */
    private void handleChannelProfileCommand(String channelId) {
        ActorRef channelProfileActor = getContext().actorOf(ChannelProfileActor.props(youTubeService));
        channelProfileActor.tell(new ChannelProfileActor.FetchChannelProfile(channelId), self());
        getContext().watch(channelProfileActor);
    }

    private void sendResultsToClient(List<Map<String, String>> results) {
        JsonNode jsonResults = formatResultsAsJson(results);
        webSocket.tell(jsonResults, self());
    }

    /**
     * Handles the results of a YouTube search.
     *
     * @param wrapper the wrapper containing the search query and results
     */
    private void handleSearchResults(SearchWrapper wrapper) {
        wrapper.futureResults.thenAcceptAsync(results -> {
            if (results != null && !results.isEmpty()) {

                List<String> descriptions = results.stream()
                        .map(result -> result.get("description"))
                        .filter(description -> description != null && !description.isEmpty())
                        .collect(Collectors.toList());

                if (!descriptions.isEmpty()) {
                    ActorRef sentimentActor = getContext().actorOf(SentimentActor.props(new SentimentAnalyser(), self()));
                    sentimentActor.tell(new SentimentActor.SentimentRequest(wrapper.query, descriptions), self());

                    ActorRef readabilityScoreActor = getContext().actorOf(ReadabilityScoreActor.props(new ReadabilityScoreUtil(), self()));
                    readabilityScoreActor.tell(new ReadabilityScoreActor.ScoreRequest(wrapper.query, descriptions), self());

                    ActorRef wordStatActor = getContext().actorOf(WordStatActor.props(self()));
                    wordStatActor.tell(new WordStatActor.WordStatRequest(wrapper.query, descriptions), self());

                    Map<String, Object> aggregatedData = new HashMap<>();
                    aggregatedData.put("results", results);
                    aggregatedData.put("scoresPending", 2); // Two responses are awaited: scores and word stats

                    getContext().become(receiveBuilder()
                            .match(SentimentActor.SentimentResponse.class, this::handleSentimentResults)
                            .match(ReadabilityScoreActor.ScoreResponse.class, scoreResponse -> {
                                List<Map<String, String>> resultList = (List<Map<String, String>>) aggregatedData.get("results");
                                mergeScoresWithResults(resultList, scoreResponse.scores);

                                double overallGradeLevel = scoreResponse.getOverallGradeLevel();
                                double overallEaseScore = scoreResponse.getOverallEaseScore();

                                Map<String, String> overallScoresEntry = new HashMap<>();
                                overallScoresEntry.put("overallGradeLevel", String.valueOf(overallGradeLevel));
                                overallScoresEntry.put("overallEaseScore", String.valueOf(overallEaseScore));
                                resultList.add(0, overallScoresEntry);

                                decrementPendingCountAndCheck(aggregatedData);
                            })
                            .match(WordStatActor.WordStatResponse.class, wordStatResponse -> {
                                List<Map<String, String>> resultList = (List<Map<String, String>>) aggregatedData.get("results");

                                // Merge the word stats only once
                                mergeWordStatsWithResults(resultList, wordStatResponse.wordStats);

                                decrementPendingCountAndCheck(aggregatedData);
                            })
                            .matchAny(o -> System.err.println("Unexpected message while waiting for responses: " + o))
                            .build(), true);
                }
            } else {
                ObjectNode emptyResponse = Json.newObject();
                emptyResponse.put("query", wrapper.query);
                emptyResponse.put("message", "No results found.");
                webSocket.tell(emptyResponse, self());
            }
        });
    }

    /**
     * Merges the readability scores into the results list.
     *
     * @param results the list of video metadata
     * @param scores  the list of readability scores
     */
    private void mergeScoresWithResults(List<Map<String, String>> results, List<ReadabilityScores> scores) {
        for (int i = 0; i < results.size() && i < scores.size(); i++) {
            Map<String, String> result = results.get(i);
            ReadabilityScores score = scores.get(i);

            result.put("gradeLevel", String.valueOf(score.getGradeLevel()));
            result.put("readingEaseScore", String.valueOf(score.getReadingEase()));
        }
    }

    private void mergeWordStatsWithResults(List<Map<String, String>> results, Map<String, Long> wordStats) {
        Map<String, String> wordStatsEntry = new HashMap<>();
        wordStatsEntry.put("wordStats", wordStats.toString());

        // Add word stats as a standalone entry
        results.add(wordStatsEntry);

        // Alternatively, you can attach word stats to the first result only:
        // if (!results.isEmpty()) {
        //     results.get(0).put("wordStats", wordStats.toString());
        // }
    }

    private void decrementPendingCountAndCheck(Map<String, Object> aggregatedData) {
        int pendingCount = (int) aggregatedData.get("scoresPending");
        pendingCount--;
        aggregatedData.put("scoresPending", pendingCount);

        if (pendingCount == 0) {
            List<Map<String, String>> results = (List<Map<String, String>>) aggregatedData.get("results");
            sendResultsToClient(results);

            getContext().unbecome(); // Revert to the original behavior
        }
    }

    /**
     * Handles the results of fetching a channel profile.
     *
     * @param response the response containing the channel profile data or an error
     */
    private void handleChannelProfileResults(ChannelProfileActor.ChannelProfileResponse response) {
        ObjectNode jsonResponse = Json.newObject();

        if (response.error != null) {
            jsonResponse.put("error", response.error);
        } else {
            jsonResponse.set("profileData", response.profileData);
        }

        webSocket.tell(jsonResponse, self());
    }

    /**
     * Handles tag-related requests and sends the tag back to the client.
     *
     * @param request the tag details request
     */
    private void handleTagDetails(TagDetailsRequest request) {
        webSocket.tell("You clicked on the tag: " + request.tag, self());
    }

    private void handleScoreResults(ReadabilityScoreActor.ScoreResponse response) {
        // Create a JSON object for the response
        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put("query", response.query);

        // Handle list of readability scores
        ArrayNode scoresArray = Json.newArray();
        response.scores.forEach(score -> {
            ObjectNode scoreObject = Json.newObject();
            scoreObject.put("gradeLevel", score.getGradeLevel());
            scoreObject.put("readingEaseScore", score.getReadingEase());
            scoresArray.add(scoreObject);
        });
        jsonResponse.set("scores", scoresArray);

        // Send the JSON response via the WebSocket
        webSocket.tell(jsonResponse, self());
    }

    /**
     * Handles the results of sentiment analysis.
     *
     * @param response the sentiment analysis results
     */
    private void handleSentimentResults(SentimentActor.SentimentResponse response) {
        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put("query", response.query);
        jsonResponse.put("overallSentiment", response.overallSentiment);

        ArrayNode sentimentsArray = Json.newArray();
        response.sentiments.forEach(sentimentsArray::add);
        jsonResponse.set("sentiments", sentimentsArray);

        webSocket.tell(jsonResponse, self());
    }

    /**
     * Handles the termination of child actors.
     *
     * @param terminated the terminated actor reference
     */
    private void handleActorTermination(Terminated terminated) {
        youtubeSearchActors.values().remove(terminated.actor());
    }

    /**
     * Converts a list of video metadata maps into a JSON array for communication with the WebSocket client.
     *
     * @param results the list of video metadata
     * @return a JSON array
     */
    private JsonNode formatResultsAsJson(List<Map<String, String>> results) {
        return Json.toJson(results);
    }

    /**
     * Custom message class for associating tags with their request.
     */
    public static class TagDetailsRequest {
        public final String tag;

        public TagDetailsRequest(String tag) {
            this.tag = tag;
        }
    }

    /**
     * Wrapper class to associate a search query with its corresponding CompletableFuture results.
     */
    static class SearchWrapper {
        final String query;
        final CompletableFuture<List<Map<String, String>>> futureResults;

        public SearchWrapper(String query, CompletableFuture<List<Map<String, String>>> futureResults) {
            this.query = query;
            this.futureResults = futureResults;
        }
    }
}
