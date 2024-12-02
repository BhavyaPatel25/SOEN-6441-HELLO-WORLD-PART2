package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import services.YouTubeService;

import java.util.Map;

/**
 * Actor responsible for fetching video details using the {@link YouTubeService}.
 */
public class VideoDetailsActor extends AbstractActor {
    private final YouTubeService youTubeService;

    /**
     * Constructs a VideoDetailsActor instance.
     *
     * @param youTubeService The YouTubeService used to fetch video details.
     */
    public VideoDetailsActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Creates the Props for the VideoDetailsActor.
     *
     * @param youTubeService The YouTubeService to be used by the actor.
     * @return Props for creating the VideoDetailsActor.
     */
    public static Props props(YouTubeService youTubeService) {
        return Props.create(VideoDetailsActor.class, () -> new VideoDetailsActor(youTubeService));
    }

    /**
     * Defines the behavior of the actor, specifying how messages are handled.
     *
     * @return Receive object defining the actor's message-handling behavior.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchVideoDetails.class, this::handleFetchVideoDetails)
                .build();
    }

    /**
     * Handles the {@link FetchVideoDetails} message by fetching video details
     * and sending a {@link VideoDetailsResponse} back to the sender.
     *
     * @param request The FetchVideoDetails message containing the video ID.
     */
    private void handleFetchVideoDetails(FetchVideoDetails request) {
        Map<String, String> videoDetails = youTubeService.getVideoDetails(request.videoId);
        sender().tell(new VideoDetailsResponse(request.videoId, videoDetails), self());
    }

    /**
     * Message class for requesting video details.
     */
    public static class FetchVideoDetails {
        /**
         * The ID of the video whose details are being requested.
         */
        public final String videoId;

        /**
         * Constructs a FetchVideoDetails message.
         *
         * @param videoId The ID of the video to fetch details for.
         */
        public FetchVideoDetails(String videoId) {
            this.videoId = videoId;
        }
    }

    /**
     * Message class for responding with video details.
     */
    public static class VideoDetailsResponse {
        /**
         * The ID of the video for which details are provided.
         */
        public final String videoId;

        /**
         * The details of the video as a map of key-value pairs.
         */
        public final Map<String, String> videoDetails;

        /**
         * Constructs a VideoDetailsResponse message.
         *
         * @param videoId      The ID of the video.
         * @param videoDetails A map containing the details of the video.
         */
        public VideoDetailsResponse(String videoId, Map<String, String> videoDetails) {
            this.videoId = videoId;
            this.videoDetails = videoDetails;
        }
    }
}
