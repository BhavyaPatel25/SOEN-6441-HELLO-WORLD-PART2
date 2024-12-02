package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TagActorTest {

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("TagActorTestSystem");
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testValidTagRequest() {
        new TestKit(system) {{
            // Arrange
            ActorRef tagActor = system.actorOf(TagActor.props());
            TagActor.TagRequest request = new TagActor.TagRequest("technology");

            // Act
            tagActor.tell(request, getRef());

            // Assert
            TagActor.TagResponse response = expectMsgClass(TagActor.TagResponse.class);
            assertEquals("technology", response.tagName);
            assertEquals(10, response.videoCount);
            assertEquals(null, response.errorMessage);
        }};
    }

    @Test
    public void testInvalidTagRequest() {
        new TestKit(system) {{
            // Arrange
            ActorRef tagActor = system.actorOf(TagActor.props());
            TagActor.TagRequest request = new TagActor.TagRequest("");

            // Act
            tagActor.tell(request, getRef());

            // Assert
            TagActor.TagResponse response = expectMsgClass(TagActor.TagResponse.class);
            assertEquals(null, response.tagName);
            assertEquals(0, response.videoCount);
            assertEquals("Invalid tag name", response.errorMessage);
        }};
    }
}
