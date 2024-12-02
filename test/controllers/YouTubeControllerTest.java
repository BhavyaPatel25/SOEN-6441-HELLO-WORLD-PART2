package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.inject.Bindings;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.test.WithApplication;
import services.YouTubeService;

import java.util.*;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/**
 * Unit tests for the {@link YouTubeController} class.
 *
 * <p>These tests validate the functionality of the controller methods for rendering
 * the search page and establishing WebSocket connections. The tests use mocked dependencies
 * for the {@link YouTubeService} and other required components.</p>
 *
 * <p>The Play Framework's {@code WithApplication} base class is extended to provide
 * a testing environment with dependency injection.</p>
 *
 * <p>Scenarios tested include search page rendering and WebSocket connection initialization.</p>
 */
public class YouTubeControllerTest extends WithApplication {

    private static ActorSystem actorSystem;
    private static Materializer materializer;
    private YouTubeService youtubeServiceMock;

    @BeforeClass
    public static void setupClass() {
        actorSystem = ActorSystem.create();
        materializer = mock(Materializer.class);
    }

    @AfterClass
    public static void teardownClass() {
        actorSystem.terminate();
    }

    @Override
    protected Application provideApplication() {
        youtubeServiceMock = mock(YouTubeService.class);

        return new GuiceApplicationBuilder()
                .overrides(
                        Bindings.bind(ActorSystem.class).toInstance(actorSystem),
                        Bindings.bind(Materializer.class).toInstance(materializer),
                        Bindings.bind(YouTubeService.class).toInstance(youtubeServiceMock)
                )
                .build();
    }

    @Test
    public void testSearchPageRendering() {
        Result result = new YouTubeController(actorSystem, materializer, youtubeServiceMock).search();
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("YT Lytics"));
    }

    @Test
    public void testWebSocketConnection() {
        WebSocket webSocket = new YouTubeController(actorSystem, materializer, youtubeServiceMock).ws();
        assertNotNull(webSocket);
    }

    @Test
    public void testFetchChannelProfileSuccess() {
        String channelId = "testChannelId";
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", channelId);
        when(youtubeServiceMock.fetchChannelProfile(channelId)).thenReturn(completedFuture(profileData));

        CompletionStage<Result> resultStage = new YouTubeController(actorSystem, materializer, youtubeServiceMock)
                .fetchChannelProfile(channelId);

        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
    }

    @Test
    public void testFetchChannelProfileFailure() {
        String channelId = "invalidChannelId";
        when(youtubeServiceMock.fetchChannelProfile(channelId)).thenReturn(completedFuture(null));

        CompletionStage<Result> resultStage = new YouTubeController(actorSystem, materializer, youtubeServiceMock)
                .fetchChannelProfile(channelId);

        Result result = resultStage.toCompletableFuture().join();
        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Channel profile data is not available."));
    }

    @Test
    public void testGetVideosByTag() {
        String tagName = "testTag";
        when(youtubeServiceMock.searchVideosAsync(tagName)).thenReturn(completedFuture(new ArrayList<>()));

        CompletionStage<Result> resultStage = new YouTubeController(actorSystem, materializer, youtubeServiceMock)
                .getVideosByTag(tagName);

        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(tagName));
    }

    @Test
    public void testGetTagPage() {
        String tagName = "sampleTag";
        Result result = new YouTubeController(actorSystem, materializer, youtubeServiceMock).getTagPage(tagName);
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(tagName));
    }



    @Test
    public void testGetWordStats() {
        String query = "testQuery";
        Map<String, Integer> wordStats = new HashMap<>();
        wordStats.put("word1", 10);
        when(youtubeServiceMock.fetchWordStatsByQuery(query)).thenReturn(completedFuture(wordStats));

        CompletionStage<Result> resultStage = new YouTubeController(actorSystem, materializer, youtubeServiceMock)
                .getWordStats(query);

        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(query));
    }

    @Test
    public void testDebugCookies() {
        // Arrange: Mock the Http.Request object and set up cookies
        Http.Request requestMock = mock(Http.Request.class);
        Http.Cookie cookie1 = new Http.Cookie(
                "testCookie1",
                "value1",
                null,  // maxAge
                "/",   // path
                "localhost", // domain
                false, // secure
                false, // httpOnly
                Http.Cookie.SameSite.LAX // sameSite
        );
        Http.Cookie cookie2 = new Http.Cookie(
                "testCookie2",
                "value2",
                null,
                "/",
                "localhost",
                false,
                false,
                Http.Cookie.SameSite.LAX
        );
        Set<Http.Cookie> cookies = Set.of(cookie1, cookie2);

        when(requestMock.cookies()).thenReturn(new Http.Cookies() {
            @Override
            public Optional<Http.Cookie> get(String name) {
                return cookies.stream().filter(cookie -> cookie.name().equals(name)).findFirst();
            }

            @Override
            public Iterator<Http.Cookie> iterator() {
                return cookies.iterator();
            }
        });

        // Act: Call the debugCookies method
        Result result = new YouTubeController(actorSystem, materializer, youtubeServiceMock).debugCookies(requestMock);

        // Assert: Validate that the result is correct
        assertEquals(OK, result.status());
        assertEquals("Check server logs for cookies.", contentAsString(result));

        // Verify that the `cookies()` method was called
        verify(requestMock).cookies();
    }
}