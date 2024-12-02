package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

/**
 * Actor responsible for handling requests related to video tags.
 *
 * <p>The {@code TagActor} processes requests to retrieve information about a specific tag, such as the
 * number of videos associated with it. It responds with a {@link TagResponse} message.</p>
 */
public class TagActor extends AbstractActor {

    /**
     * Creates a {@link Props} instance for the {@code TagActor}.
     *
     * @return a {@link Props} instance for creating a {@code TagActor} instance
     */
    public static Props props() {
        return Props.create(TagActor.class);
    }

    // Messages

    /**
     * Message class representing a request to retrieve information about a tag.
     *
     * <p>This message contains the tag name for which information is requested.</p>
     */
    public static class TagRequest {
        public final String tagName;

        /**
         * Constructs a {@code TagRequest} instance.
         *
         * @param tagName the name of the tag to retrieve information about
         */
        public TagRequest(String tagName) {
            this.tagName = tagName;
        }
    }

    /**
     * Message class representing the response containing information about a tag.
     *
     * <p>This message contains the tag name, the number of videos associated with it,
     * and any error message if applicable.</p>
     */
    public static class TagResponse {
        public final String tagName;
        public final int videoCount;
        public final String errorMessage;

        /**
         * Constructs a {@code TagResponse} instance.
         *
         * @param tagName      the name of the tag
         * @param videoCount   the number of videos associated with the tag
         * @param errorMessage an error message, if any, related to the request
         */
        public TagResponse(String tagName, int videoCount, String errorMessage) {
            this.tagName = tagName;
            this.videoCount = videoCount;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Defines the actor's behavior by specifying how it reacts to incoming messages.
     *
     * @return a {@link Receive} object defining the message handling logic
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TagRequest.class, request -> {
                    if (request.tagName == null || request.tagName.isEmpty()) {
                        // If tag name is invalid, respond with an error
                        getSender().tell(new TagResponse(null, 0, "Invalid tag name"), getSelf());
                    } else {
                        // Mock response for testing purposes
                        getSender().tell(new TagResponse(request.tagName, 10, null), getSelf());
                    }
                })
                .build();
    }
}
