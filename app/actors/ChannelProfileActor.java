package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import services.YouTubeService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Actor responsible for fetching and returning YouTube channel profile data.
 * <p>
 * This actor communicates with the {@link YouTubeService} to fetch channel profile data
 * and sends the response back to the sender.
 */
public class ChannelProfileActor extends AbstractActor {

    private final YouTubeService youtubeService;

    /**
     * Constructor for ChannelProfileActor.
     *
     * @param youtubeService the YouTubeService used to fetch channel profile data
     */
    public ChannelProfileActor(YouTubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    /**
     * Creates a {@link Props} instance for the ChannelProfileActor.
     *
     * @param youtubeService the YouTubeService used to fetch channel profile data
     * @return a Props instance to create the actor
     */
    public static Props props(YouTubeService youtubeService) {
        return Props.create(ChannelProfileActor.class, youtubeService);
    }

    /**
     * Defines the actor's behavior by specifying how it reacts to incoming messages.
     *
     * @return a {@link Receive} object defining the message handling logic
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchChannelProfile.class, this::handleFetchChannelProfile)
                .build();
    }

    /**
     * Handles the {@link FetchChannelProfile} message by invoking the {@link YouTubeService} to fetch profile data.
     * Once the data is fetched, it sends a {@link ChannelProfileResponse} message back to the sender.
     *
     * @param request the {@link FetchChannelProfile} message containing the channel ID
     */
    private void handleFetchChannelProfile(FetchChannelProfile request) {
        CompletableFuture<Map<String, Object>> profileFuture = youtubeService.fetchChannelProfile(request.channelId);

        profileFuture.thenAccept(profileData -> {
            ChannelProfileResponse response;
            if (profileData.isEmpty()) {
                response = new ChannelProfileResponse(request.channelId, null, "Channel profile not found");
            } else {
                JsonNode jsonResponse = Json.toJson(profileData);
                response = new ChannelProfileResponse(request.channelId, jsonResponse, null);
            }
            sender().tell(response, self());
        });
    }

    /**
     * Message class representing a request to fetch a YouTube channel profile.
     */
    public static class FetchChannelProfile {
        /**
         * The ID of the YouTube channel.
         */
        public final String channelId;

        /**
         * Constructor for {@link FetchChannelProfile}.
         *
         * @param channelId the ID of the YouTube channel to fetch the profile for
         */
        public FetchChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    /**
     * Message class representing a response containing the YouTube channel profile data.
     */
    public static class ChannelProfileResponse {
        /**
         * The ID of the YouTube channel.
         */
        public final String channelId;

        /**
         * The profile data of the YouTube channel as a JSON object.
         * This is null if there was an error or the channel was not found.
         */
        public final JsonNode profileData;

        /**
         * An error message if the channel profile could not be fetched.
         * This is null if the request was successful.
         */
        public final String error;

        /**
         * Constructor for {@link ChannelProfileResponse}.
         *
         * @param channelId   the ID of the YouTube channel
         * @param profileData the profile data of the channel as a JSON object
         * @param error       an error message if the profile could not be fetched
         */
        public ChannelProfileResponse(String channelId, JsonNode profileData, String error) {
            this.channelId = channelId;
            this.profileData = profileData;
            this.error = error;
        }
    }
}
