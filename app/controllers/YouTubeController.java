package controllers;

import actors.UserActor;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.*;
import services.YouTubeService;
import views.html.channelProfile;
import views.html.search;
import views.html.searchResults;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Controller for managing YouTube-related functionalities.
 */
public class YouTubeController extends Controller {

    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final YouTubeService youtubeService;

    /**
     * Constructor for the YouTubeController.
     *
     * @param actorSystem    ActorSystem for handling concurrency.
     * @param materializer   Materializer for managing streams.
     * @param youtubeService Service for interacting with the YouTube API.
     */
    @Inject
    public YouTubeController(ActorSystem actorSystem, Materializer materializer, YouTubeService youtubeService) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.youtubeService = youtubeService;
    }

    /**
     * Renders the search page for YouTube videos.
     *
     * @return Rendered HTML for the search page.
     */
    public Result search() {
        return ok(search.render("YT Lytics - Search Page"));
    }

    /**
     * Provides a WebSocket endpoint for real-time updates related to YouTube video searches.
     *
     * @return A WebSocket instance configured for JSON message handling.
     */
    public WebSocket ws() {
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(
                ws -> UserActor.props(ws, youtubeService),
                actorSystem,
                materializer
        ));
    }

    /**
     * Fetches and displays the channel profile for a given channel ID.
     *
     * @param channelId The ID of the YouTube channel to fetch.
     * @return Rendered HTML for the channel profile page.
     */
    public CompletionStage<Result> fetchChannelProfile(String channelId) {
        return youtubeService.fetchChannelProfile(channelId)
                .thenApply(profileData -> {
                    if (profileData == null || profileData.isEmpty()) {
                        return badRequest("Channel profile data is not available.");
                    }
                    return ok(channelProfile.render(Json.toJson(profileData)));
                });
    }

    public CompletionStage<Result> getWordStats(String query) {
        return youtubeService.fetchWordStatsByQuery(query)
                .thenApply(wordStats -> ok(views.html.wordStats.render(query, wordStats)));
    }



    /**
     * Fetches and displays videos associated with a specific tag.
     *
     * @param tagName The tag to search for videos.
     * @return Rendered HTML for videos related to the tag.
     */
    public CompletionStage<Result> getVideosByTag(String tagName) {
        return youtubeService.searchVideosAsync(tagName)
                .thenApplyAsync(videos -> ok(searchResults.render(tagName, videos)));
    }

    /**
     * Debugging endpoint to view cookies.
     *
     * @param request The HTTP request.
     * @return A response displaying all cookies.
     */
    public Result debugCookies(Http.Request request) {
        request.cookies().forEach(cookie -> {
            System.out.println("Cookie: " + cookie.name() + " = " + cookie.value());
        });
        return ok("Check server logs for cookies.");
    }
}
