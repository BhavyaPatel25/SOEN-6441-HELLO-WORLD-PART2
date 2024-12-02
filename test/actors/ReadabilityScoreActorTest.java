package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utils.ReadabilityScores;
import utils.ReadabilityScoreUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadabilityScoreActorTest {

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("ReadabilityScoreActorTestSystem");
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testHandleScoreRequestWithValidInput() {
        new TestKit(system) {{
            // Arrange
            ReadabilityScoreUtil readabilityScoreUtil = new ReadabilityScoreUtil();
            ActorRef readabilityScoreActor = system.actorOf(ReadabilityScoreActor.props(readabilityScoreUtil, getRef()));

            String query = "validTest";
            List<String> descriptions = Arrays.asList(
                    "This is a simple test.",
                    "Another test with more complexity.",
                    "Readability scores are being calculated."
            );
            ReadabilityScoreActor.ScoreRequest request = new ReadabilityScoreActor.ScoreRequest(query, descriptions);

            // Act
            readabilityScoreActor.tell(request, getRef());

            // Assert
            ReadabilityScoreActor.ScoreResponse response = expectMsgClass(ReadabilityScoreActor.ScoreResponse.class);
            assertEquals(query, response.query, "The query in the response should match the request.");
            assertEquals(3, response.scores.size(), "There should be scores for each description.");
            assertEquals(3, response.scores.size(), "Each description should generate a score.");
        }};
    }

    @Test
    public void testHandleScoreRequestWithException() {
        new TestKit(system) {{
            // Arrange
            ReadabilityScoreUtil readabilityScoreUtil = new ReadabilityScoreUtil();
            ActorRef readabilityScoreActor = system.actorOf(ReadabilityScoreActor.props(readabilityScoreUtil, getRef()));

            String query = "exceptionTest";
            List<String> descriptions = Arrays.asList("Valid text", null); // Include a null description to cause an exception

            ReadabilityScoreActor.ScoreRequest request = new ReadabilityScoreActor.ScoreRequest(query, descriptions);

            // Act
            readabilityScoreActor.tell(request, getRef());

            // Assert
            ReadabilityScoreActor.ScoreResponse response = expectMsgClass(ReadabilityScoreActor.ScoreResponse.class);
            assertEquals(query, response.query, "The query in the response should match the request.");
            assertEquals(0, response.scores.size(), "The scores list should be empty due to the exception.");
            assertEquals(0.0, response.getOverallGradeLevel(), "The overall grade level should be 0.0 due to the exception.");
            assertEquals(0.0, response.getOverallEaseScore(), "The overall ease score should be 0.0 due to the exception.");
        }};
    }

    @Test
    public void testHandleScoreRequestWithEmptyDescriptions() {
        new TestKit(system) {{
            // Arrange
            ReadabilityScoreUtil readabilityScoreUtil = new ReadabilityScoreUtil();
            ActorRef readabilityScoreActor = system.actorOf(ReadabilityScoreActor.props(readabilityScoreUtil, getRef()));

            String query = "emptyDescriptionsTest";
            ReadabilityScoreActor.ScoreRequest request = new ReadabilityScoreActor.ScoreRequest(query, Collections.emptyList());

            // Act
            readabilityScoreActor.tell(request, getRef());

            // Assert
            ReadabilityScoreActor.ScoreResponse response = expectMsgClass(ReadabilityScoreActor.ScoreResponse.class);
            assertEquals(query, response.query, "The query in the response should match the request.");
            assertEquals(0, response.scores.size(), "The scores list should be empty.");
            assertEquals(0.0, response.getOverallGradeLevel(), "The overall grade level should be 0.0.");
            assertEquals(0.0, response.getOverallEaseScore(), "The overall ease score should be 0.0.");
        }};
    }


    @Test
    public void testScoreResponseToString() {
        // Arrange
        String query = "toStringTest";
        List<ReadabilityScores> scores = Arrays.asList(
                new ReadabilityScores(5.5, 80.0),
                new ReadabilityScores(6.0, 75.0)
        );
        ReadabilityScoreActor.ScoreResponse response = new ReadabilityScoreActor.ScoreResponse(query, scores, 5.75, 77.5);

        // Act
        String actualToString = response.toString();

        // Assert
        String expectedToString = "ScoreResponse{query='toStringTest', scores=" + scores +
                ", overallGradeLevel=5.75, overallEaseScore=77.5}";
        assertEquals(expectedToString, actualToString, "The toString method should return the correct string representation.");
    }
}
