package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import play.libs.Json;
import services.YouTubeService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ChannelProfileActorTest {

    static ActorSystem system;
    static YouTubeService mockYouTubeService;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("ChannelProfileActorTestSystem");
        mockYouTubeService = Mockito.mock(YouTubeService.class);
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testFetchChannelProfileSuccess() {
        new TestKit(system) {{
            // Arrange
            ActorRef channelProfileActor = system.actorOf(ChannelProfileActor.props(mockYouTubeService));
            String channelId = "channel123";
            Map<String, Object> mockProfileData = new HashMap<>();
            mockProfileData.put("name", "Test Channel");
            mockProfileData.put("subscribers", 5000);

            when(mockYouTubeService.fetchChannelProfile(channelId))
                    .thenReturn(CompletableFuture.completedFuture(mockProfileData));

            ChannelProfileActor.FetchChannelProfile request = new ChannelProfileActor.FetchChannelProfile(channelId);

            // Act
            channelProfileActor.tell(request, getRef());

            // Assert
            ChannelProfileActor.ChannelProfileResponse response = expectMsgClass(ChannelProfileActor.ChannelProfileResponse.class);
            assertEquals(channelId, response.channelId);
            assertEquals("Test Channel", response.profileData.get("name").asText());
            assertEquals(5000, response.profileData.get("subscribers").asInt());
            assertEquals(null, response.error);
        }};
    }

    @Test
    public void testFetchChannelProfileNotFound() {
        new TestKit(system) {{
            // Arrange
            ActorRef channelProfileActor = system.actorOf(ChannelProfileActor.props(mockYouTubeService));
            String channelId = "channelNotFound";
            when(mockYouTubeService.fetchChannelProfile(channelId))
                    .thenReturn(CompletableFuture.completedFuture(Collections.emptyMap()));

            ChannelProfileActor.FetchChannelProfile request = new ChannelProfileActor.FetchChannelProfile(channelId);

            // Act
            channelProfileActor.tell(request, getRef());

            // Assert
            ChannelProfileActor.ChannelProfileResponse response = expectMsgClass(ChannelProfileActor.ChannelProfileResponse.class);
            assertEquals(channelId, response.channelId);
            assertEquals(null, response.profileData);
            assertEquals("Channel profile not found", response.error);
        }};
    }



}
