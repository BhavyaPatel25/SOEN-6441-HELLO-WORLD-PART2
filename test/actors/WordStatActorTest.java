package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WordStatActorTest {

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("WordStatActorTestSystem");
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testHandleWordStatRequestWithValidInput() {
        new TestKit(system) {{
            // Arrange
            ActorRef wordStatActor = system.actorOf(WordStatActor.props(getRef()));
            String query = "testQuery";
            WordStatActor.WordStatRequest request = new WordStatActor.WordStatRequest(
                    query,
                    Arrays.asList("This is a test.", "Test the actor!", "Another test here.")
            );

            // Act
            wordStatActor.tell(request, getRef());

            // Assert
            WordStatActor.WordStatResponse response = expectMsgClass(WordStatActor.WordStatResponse.class);
            assertEquals(query, response.query, "The query in the response should match the request.");
            Map<String, Long> expectedStats = new HashMap<>();
            expectedStats.put("test", 3L);
            expectedStats.put("this", 1L);
            expectedStats.put("is", 1L);
            expectedStats.put("a", 1L);
            expectedStats.put("the", 1L);
            expectedStats.put("actor", 1L);
            expectedStats.put("another", 1L);
            expectedStats.put("here", 1L);
            assertEquals(expectedStats, response.wordStats, "The word statistics should match the expected values.");
        }};
    }

    @Test
    public void testHandleWordStatRequestWithException() {
        new TestKit(system) {{
            // Arrange
            ActorRef wordStatActor = system.actorOf(WordStatActor.props(getRef()));
            String query = "exceptionTest";

            // Create descriptions that cause an exception (e.g., forcing a null element in the stream)
            List<String> descriptions = Arrays.asList("Valid description", null);

            WordStatActor.WordStatRequest request = new WordStatActor.WordStatRequest(query, descriptions);

            // Act
            wordStatActor.tell(request, getRef());

            // Assert
            WordStatActor.WordStatResponse response = expectMsgClass(WordStatActor.WordStatResponse.class);

            assertEquals(query, response.query, "The query in the response should match the request.");
            assertEquals(Collections.emptyMap(), response.wordStats, "The word statistics should be empty due to exception handling.");
        }};
    }

    @Test
    public void testWordStatResponseToString() {
        // Arrange
        String query = "sampleQuery";
        Map<String, Long> wordStats = new HashMap<>();
        wordStats.put("test", 3L);
        wordStats.put("example", 2L);
        WordStatActor.WordStatResponse response = new WordStatActor.WordStatResponse(query, wordStats);

        // Act
        String actualToString = response.toString();

        // Assert
        String expectedToString = "WordStatResponse{query='sampleQuery', wordStats={test=3, example=2}}";
        assertEquals(expectedToString, actualToString, "The toString method should return the correct string representation.");
    }

    @Test
    public void testHandleWordStatRequestWithEmptyDescriptions() {
        new TestKit(system) {{
            // Arrange
            ActorRef wordStatActor = system.actorOf(WordStatActor.props(getRef()));
            String query = "emptyDescriptions";
            WordStatActor.WordStatRequest request = new WordStatActor.WordStatRequest(query, Collections.emptyList());

            // Act
            wordStatActor.tell(request, getRef());

            // Assert
            WordStatActor.WordStatResponse response = expectMsgClass(WordStatActor.WordStatResponse.class);
            assertEquals(query, response.query, "The query in the response should match the request.");
            assertEquals(Collections.emptyMap(), response.wordStats, "The word statistics should be empty.");
        }};
    }

    @Test
    public void testHandleWordStatRequestWithNullDescriptions() {
        new TestKit(system) {{
            // Arrange
            ActorRef wordStatActor = system.actorOf(WordStatActor.props(getRef()));
            String query = "nullDescriptions";
            WordStatActor.WordStatRequest request = new WordStatActor.WordStatRequest(query, null);

            // Act
            wordStatActor.tell(request, getRef());

            // Assert
            WordStatActor.WordStatResponse response = expectMsgClass(WordStatActor.WordStatResponse.class);
            assertEquals(query, response.query, "The query in the response should match the request.");
            assertEquals(Collections.emptyMap(), response.wordStats, "The word statistics should be empty.");
        }};
    }

    @Test
    public void testHandleWordStatRequestWithSpecialCharacters() {
        new TestKit(system) {{
            // Arrange
            ActorRef wordStatActor = system.actorOf(WordStatActor.props(getRef()));
            String query = "specialCharacters";
            WordStatActor.WordStatRequest request = new WordStatActor.WordStatRequest(
                    query,
                    Arrays.asList("Hello, world!", "Test-case #123.", "Special@characters$%^&.")
            );

            // Act
            wordStatActor.tell(request, getRef());

            // Assert
            WordStatActor.WordStatResponse response = expectMsgClass(WordStatActor.WordStatResponse.class);
            assertEquals(query, response.query, "The query in the response should match the request.");
            Map<String, Long> expectedStats = new HashMap<>();
            expectedStats.put("hello", 1L);
            expectedStats.put("world", 1L);
            expectedStats.put("test", 1L);
            expectedStats.put("case", 1L);
            expectedStats.put("123", 1L);
            expectedStats.put("special", 1L);
            expectedStats.put("characters", 1L);
            assertEquals(expectedStats, response.wordStats, "The word statistics should match the expected values.");
        }};
    }
}
