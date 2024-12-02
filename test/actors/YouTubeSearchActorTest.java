package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import services.YouTubeService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link YouTubeSearchActor} class.
 *
 * <p>These tests verify the behavior of the {@code YouTubeSearchActor} in fetching
 * videos, handling various responses from the YouTube API, and sending appropriate messages
 * to the {@code UserActor}. It uses Akka's {@code TestKit} for actor-based testing and Mockito
 * for mocking the YouTube service.</p>
 *
 * <p>Scenarios tested include successful video fetching, no results found, and handling of errors.</p>
 */
public class YouTubeSearchActorTest {

    private static ActorSystem system;

    /**
     * Sets up the actor system before running the tests.
     *
     * <p>This ensures a shared actor system is available for all test cases.</p>
     *
     * @author Devang Shah
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    /**
     * Tears down the actor system after all tests are complete.
     *
     * <p>This releases resources associated with the actor system.</p>
     *
     * @author Devang Shah
     */
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests the behavior of the {@code YouTubeSearchActor} when successfully fetching videos.
     *
     * <p>This test simulates a successful video search and verifies that the correct video results
     * are sent to the {@code UserActor}. It also checks that a sentiment analysis request is made.</p>
     *
     * @author Devang Shah
     */
    @Test
    public void testFetchVideosSuccess() {
        new TestKit(system) {{
            // Mock YouTubeService
            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);

            // Define mock response
            List<Map<String, String>> mockResults = List.of(
                    Map.of(
                            "title", "Video 1",
                            "description", "A great video about learning.",
                            "videoId", "123",
                            "channelTitle", "Channel 1",
                            "channelId", "channel123",
                            "defaultThumbnail", "http://example.com/thumb1.jpg"
                    ),
                    Map.of(
                            "title", "Video 2",
                            "description", "Another amazing video.",
                            "videoId", "456",
                            "channelTitle", "Channel 2",
                            "channelId", "channel456",
                            "defaultThumbnail", "http://example.com/thumb2.jpg"
                    )
            );

            // Stub the searchVideosAsync method
            when(mockYouTubeService.searchVideosAsync(anyString()))
                    .thenReturn(CompletableFuture.completedFuture(mockResults));

            // Create UserActor mock
            ActorRef userActor = getRef();

            // Create YouTubeSearchActor
            ActorRef youtubeSearchActor = system.actorOf(YouTubeSearchActor.props("test query", mockYouTubeService, userActor));

            // Expect the results sent to UserActor
            expectMsgClass(JsonNode.class);
            expectMsgClass(SentimentActor.SentimentRequest.class);
        }};
    }

    /**
     * Tests the behavior of the {@code YouTubeSearchActor} when no results are returned from the YouTube API.
     *
     * <p>This test simulates a search with no results and verifies that an empty results array is
     * sent to the {@code UserActor}.</p>
     *
     * @author Devang Shah
     */
    @Test
    public void testFetchVideosNoResults() {
        new TestKit(system) {{
            // Mock YouTubeService
            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);

            // Stub the searchVideosAsync method to return an empty list
            when(mockYouTubeService.searchVideosAsync(anyString()))
                    .thenReturn(CompletableFuture.completedFuture(List.of()));

            // Create UserActor mock
            ActorRef userActor = getRef();

            // Create YouTubeSearchActor
            ActorRef youtubeSearchActor = system.actorOf(YouTubeSearchActor.props("test query", mockYouTubeService, userActor));

            // Expect an empty results array sent to UserActor
            expectMsgClass(ArrayNode.class);
        }};
    }

    /**
     * Tests the behavior of the {@code YouTubeSearchActor} when an exception is thrown during video fetching.
     *
     * <p>This test simulates an error during the API call and verifies that no message is sent to the
     * {@code UserActor} due to exception handling.</p>
     *
     * @author Devang Shah
     */
    @Test
    public void testFetchVideosWithException() {
        new TestKit(system) {{
            // Mock YouTubeService
            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);

            // Stub the searchVideosAsync method to throw an exception
            when(mockYouTubeService.searchVideosAsync(anyString()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API error")));

            // Create UserActor mock
            ActorRef userActor = getRef();

            // Create YouTubeSearchActor
            ActorRef youtubeSearchActor = system.actorOf(YouTubeSearchActor.props("test query", mockYouTubeService, userActor));

            // Expect no messages (exception handled silently)
            expectNoMessage();
        }};
    }
}
