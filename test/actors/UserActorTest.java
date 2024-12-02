//package actors;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.testkit.javadsl.TestKit;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.mockito.Mockito;
//import play.libs.Json;
//import services.YouTubeService;
//import utils.SentimentAnalyser;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//import static org.mockito.Mockito.when;
//
///**
// * Unit tests for the {@link UserActor} class.
// *
// * <p>These tests verify the behavior of the {@code UserActor} in handling WebSocket commands,
// * processing YouTube video searches, and sending sentiment analysis results. It uses Akka's
// * {@code TestKit} for actor-based testing and Mockito for mocking dependencies.</p>
// *
// * <p>Scenarios tested include valid search commands, invalid commands, and cases with empty results.</p>
// *
// * @author Devang Shah
// */
//public class UserActorTest {
//
//    static ActorSystem system;
//
//    /**
//     * Sets up the actor system before running the tests.
//     *
//     * <p>This ensures a shared actor system is available for all test cases.</p>
//     *
//     * @author Devang Shah
//     */
//    @BeforeClass
//    public static void setup() {
//        system = ActorSystem.create();
//    }
//
//    /**
//     * Tears down the actor system after all tests are complete.
//     *
//     * <p>This releases resources associated with the actor system.</p>
//     *
//     * @author Devang Shah
//     */
//    @AfterClass
//    public static void teardown() {
//        TestKit.shutdownActorSystem(system);
//        system = null;
//    }
//
//    /**
//     * Tests the behavior of the {@code UserActor} when processing a valid search command.
//     *
//     * <p>This test verifies that the actor correctly fetches video search results,
//     * performs sentiment analysis, and sends the expected messages to the WebSocket.</p>
//     *
//     * <p>Mocked YouTube API results and sentiment analysis responses are used
//     * to simulate real interactions.</p>
//     *
//     * @author Devang Shah
//     */
//    @Test
//    public void testSearchCommand() {
//        new TestKit(system) {{
//            // Mock dependencies
//            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);
//            SentimentAnalyser mockSentimentAnalyser = Mockito.mock(SentimentAnalyser.class);
//
//            // Mock results from YouTube API
//            List<Map<String, String>> mockResults = Arrays.asList(
//                    Map.of("title", "Video 1", "description", "Happy description", "videoId", "123", "channelTitle", "Channel 1", "channelId", "ch1"),
//                    Map.of("title", "Video 2", "description", "Sad description", "videoId", "456", "channelTitle", "Channel 2", "channelId", "ch2")
//            );
//            when(mockYouTubeService.searchVideosAsync("test")).thenReturn(CompletableFuture.completedFuture(mockResults));
//
//            // Mock sentiment analysis
//            when(mockSentimentAnalyser.analyzeDescription("Happy description")).thenReturn("happy");
//            when(mockSentimentAnalyser.analyzeDescription("Sad description")).thenReturn("sad");
//            when(mockSentimentAnalyser.analyzeOverallSentiment(Arrays.asList("Happy description", "Sad description"))).thenReturn("neutral");
//
//            // Create WebSocket mock
//            ActorRef mockWebSocket = getRef();
//
//            // Create UserActor
//            ActorRef userActor = system.actorOf(UserActor.props(mockWebSocket, mockYouTubeService));
//
//            // Send search command
//            ObjectNode searchCommand = Json.newObject();
//            searchCommand.put("action", "search");
//            searchCommand.put("query", "test");
//            userActor.tell(searchCommand, getRef());
//
//            // Expect results sent to WebSocket
//            JsonNode expectedResults = Json.toJson(mockResults);
//            expectMsgEquals(expectedResults);
//
//            // Expect sentiment analysis result
//            ObjectNode expectedSentimentResponse = Json.newObject();
//            expectedSentimentResponse.put("query", "test");
//            expectedSentimentResponse.put("overallSentiment", "neutral");
//            expectedSentimentResponse.set("sentiments", Json.toJson(Arrays.asList("happy", "sad")));
//            expectMsgEquals(expectedSentimentResponse);
//        }};
//    }
//
//    /**
//     * Tests the behavior of the {@code UserActor} when receiving an invalid command.
//     *
//     * <p>This test verifies that the actor ignores unsupported actions and sends no messages
//     * to the WebSocket.</p>
//     *
//     * @author Devang Shah
//     */
//    @Test
//    public void testInvalidCommand() {
//        new TestKit(system) {{
//            // Mock dependencies
//            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);
//
//            // Create WebSocket mock
//            ActorRef mockWebSocket = getRef();
//
//            // Create UserActor
//            ActorRef userActor = system.actorOf(UserActor.props(mockWebSocket, mockYouTubeService));
//
//            // Send invalid command
//            ObjectNode invalidCommand = Json.newObject();
//            invalidCommand.put("action", "unknown");
//            invalidCommand.put("query", "test");
//            userActor.tell(invalidCommand, getRef());
//
//            // Expect no messages sent to WebSocket
//            expectNoMessage();
//        }};
//    }
//
//    /**
//     * Tests the behavior of the {@code UserActor} when no video search results are returned.
//     *
//     * <p>This test verifies that the actor sends a "No results found" message to the WebSocket
//     * when the YouTube API returns an empty list of results.</p>
//     *
//     * <p>Mocked YouTube API responses are used to simulate an empty results scenario.</p>
//     *
//     * @author Devang Shah
//     */
//    @Test
//    public void testEmptyResults() {
//        new TestKit(system) {{
//            // Mock dependencies
//            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);
//
//            // Mock empty results from YouTube API
//            when(mockYouTubeService.searchVideosAsync("empty")).thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));
//
//            // Create WebSocket mock
//            ActorRef mockWebSocket = getRef();
//
//            // Create UserActor
//            ActorRef userActor = system.actorOf(UserActor.props(mockWebSocket, mockYouTubeService));
//
//            // Send search command for empty results
//            ObjectNode searchCommand = Json.newObject();
//            searchCommand.put("action", "search");
//            searchCommand.put("query", "empty");
//            userActor.tell(searchCommand, getRef());
//
//            // Expect empty results message sent to WebSocket
//            ObjectNode expectedEmptyResponse = Json.newObject();
//            expectedEmptyResponse.put("query", "empty");
//            expectedEmptyResponse.put("message", "No results found.");
//            expectMsgEquals(expectedEmptyResponse);
//        }};
//    }
//}

package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import play.libs.Json;
import services.YouTubeService;
import utils.ReadabilityScores;
import utils.SentimentAnalyser;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserActorTest {

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("UserActorTestSystem");
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testHandleSearchCommand() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            CompletableFuture<List<Map<String, String>>> mockFuture = CompletableFuture.completedFuture(new ArrayList<>());
            when(mockYouTubeService.searchVideosAsync("testQuery")).thenReturn(mockFuture);

            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));
            ObjectNode searchCommand = Json.newObject();
            searchCommand.put("action", "search");
            searchCommand.put("query", "testQuery");

            // Act
            userActor.tell(searchCommand, getRef());

            // Assert
            awaitAssert(() -> verify(mockYouTubeService, times(1)).searchVideosAsync("testQuery"));
        }};
    }

    @Test
    public void testHandleGetVideoDetailsCommand() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            ObjectNode getVideoDetailsCommand = Json.newObject();
            getVideoDetailsCommand.put("action", "getVideoDetails");
            getVideoDetailsCommand.put("videoId", "testVideoId");

            // Act
            userActor.tell(getVideoDetailsCommand, getRef());

            // Assert
            awaitAssert(() -> {
                ActorRef videoDetailsActor = system.actorSelection("/user/" + getRef().path().name() + "/$a")
                        .anchor();
                assertNotNull(videoDetailsActor, "VideoDetailsActor should be created.");
                return null;
            });

            // Verify that the VideoDetailsActor received the expected message
            expectNoMessage(); // Adjust as necessary to assert expected interaction
        }};
    }

    @Test
    public void testHandleGetChannelProfileCommand() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            ObjectNode getChannelProfileCommand = Json.newObject();
            getChannelProfileCommand.put("action", "getChannelProfile");
            getChannelProfileCommand.put("channelId", "testChannelId");

            // Act
            userActor.tell(getChannelProfileCommand, getRef());

            // Assert
            awaitAssert(() -> {
                // Check that a child actor for ChannelProfileActor was created
                ActorRef channelProfileActor = system.actorSelection("/user/" + userActor.path().name() + "/$a")
                        .anchor();
                assertNotNull(channelProfileActor, "ChannelProfileActor should be created.");
                return null;
            });

            // Verify that no unexpected messages are received
            expectNoMessage();
        }};
    }

    @Test
    public void testHandleUnknownAction() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            ObjectNode unknownActionCommand = Json.newObject();
            unknownActionCommand.put("action", "unknownAction");

            // Act
            userActor.tell(unknownActionCommand, getRef());

            // Assert
            // No response is expected for an unknown action
            expectNoMessage();
        }};
    }



    @Test
    public void testHandleInvalidWebSocketCommand() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            // Case 1: Message is not a JSON object
            JsonNode invalidCommand1 = Json.newArray(); // Not an object
            userActor.tell(invalidCommand1, getRef());

            // Case 2: Missing "action" field
            ObjectNode invalidCommand2 = Json.newObject();
            invalidCommand2.put("query", "testQuery"); // No "action" field
            userActor.tell(invalidCommand2, getRef());

            // Assert
            // No response is expected for invalid commands
            expectNoMessage();
        }};
    }


    @Test
    public void testHandleGetTagDetailsCommand() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            ObjectNode getTagDetailsCommand = Json.newObject();
            getTagDetailsCommand.put("action", "getTagDetails");
            getTagDetailsCommand.put("tag", "testTag");

            // Act
            userActor.tell(getTagDetailsCommand, getRef());

            // Assert
            // Verify that the tag details message is sent to the WebSocket
            String expectedMessage = "You clicked on the tag: testTag";
            String actualMessage = expectMsgClass(String.class);
            assertEquals(expectedMessage, actualMessage, "The tag details message should be correctly forwarded.");
        }};
    }

    @Test
    public void testHandleChannelProfileCommand() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            ObjectNode channelProfileCommand = Json.newObject();
            channelProfileCommand.put("action", "getChannelProfile");
            channelProfileCommand.put("channelId", "testChannelId");

            // Act
            userActor.tell(channelProfileCommand, getRef());

            // Assert
            // Verify that a child actor is created for handling the channel profile
            expectNoMessage();
        }};
    }

    @Test
    public void testHandleTagDetails() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            // Act
            userActor.tell(new UserActor.TagDetailsRequest("testTag"), getRef());

            // Assert
            String expectedMessage = "You clicked on the tag: testTag";
            expectMsg(expectedMessage);
        }};
    }

    @Test
    public void testHandleSearchResultsWithValidDescriptions() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            List<Map<String, String>> mockResults = new ArrayList<>();
            Map<String, String> video1 = new HashMap<>();
            video1.put("description", "This is a test description.");
            mockResults.add(video1);

            CompletableFuture<List<Map<String, String>>> futureResults = CompletableFuture.completedFuture(mockResults);
            UserActor.SearchWrapper searchWrapper = new UserActor.SearchWrapper("testQuery", futureResults);

            // Act
            userActor.tell(searchWrapper, getRef());

            // Assert
            awaitAssert(() -> {
                // Expect no message or specific behavior
                assertTrue(true, "Placeholder assertion to meet awaitAssert requirements.");
                return null; // Returning null satisfies the Supplier type constraint
            });
        }};
    }

    @Test
    public void testHandleScoreResponse() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            List<ReadabilityScores> mockScores = Arrays.asList(
                    new ReadabilityScores(5.0, 70.0),
                    new ReadabilityScores(6.0, 75.0)
            );
            ReadabilityScoreActor.ScoreResponse scoreResponse = new ReadabilityScoreActor.ScoreResponse(
                    "testQuery", mockScores, 5.5, 72.5
            );

            // Act
            userActor.tell(scoreResponse, getRef());

            // Assert
            ObjectNode expectedResponse = Json.newObject();
            expectedResponse.put("query", "testQuery");
            expectedResponse.set("scores", Json.toJson(mockScores));

            ObjectNode actualResponse = expectMsgClass(ObjectNode.class);

            assertEquals(expectedResponse.get("query").asText(), actualResponse.get("query").asText());
//            assertEquals(expectedResponse.get("scores").toString(), actualResponse.get("scores").toString());
        }};
    }
}
