package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.SentimentAnalyser;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link SentimentActor} class.
 *
 * <p>This test suite verifies the behavior of the {@code SentimentActor} in handling
 * sentiment analysis requests and generating correct responses for various scenarios,
 * including happy, sad, neutral sentiments, and edge cases like no descriptions.</p>
 *
 * <p>Uses the Akka {@code TestKit} for actor-based testing.</p>
 */
public class SentimentActorTest {

    private static ActorSystem system;

    /**
     * Sets up the actor system before running the tests.
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
     * <p>Ensures that resources associated with the actor system are properly cleaned up.</p>
     *
     * @author Devang Shah
     */
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests the sentiment analysis for a query with only "happy" descriptions.
     *
     * <p>Verifies that all individual sentiments and the overall sentiment are correctly
     * identified as "happy".</p>
     *
     * @throws Exception if an unexpected error occurs during the test
     * @author Devang Shah
     */
    @Test
    public void testSentimentAnalysisHappy() {
        new TestKit(system) {{
            SentimentAnalyser analyser = new SentimentAnalyser(
                    List.of("great", "awesome", "happy"),
                    List.of("sad", "bad", "terrible")
            );
            ActorRef sentimentActor = system.actorOf(SentimentActor.props(analyser, getRef()));

            List<String> descriptions = List.of(
                    "This is a great video!",
                    "What an awesome experience!",
                    "Feeling very happy today!"
            );
            sentimentActor.tell(new SentimentActor.SentimentRequest("query1", descriptions), getRef());

            SentimentActor.SentimentResponse response = expectMsgClass(SentimentActor.SentimentResponse.class);

            assertEquals("query1", response.query);
            assertEquals(":-|", response.overallSentiment);
            assertEquals(3, response.sentiments.size());
            assertEquals(":-)", response.sentiments.get(0));
            assertEquals(":-)", response.sentiments.get(1));
            assertEquals(":-)", response.sentiments.get(2));
        }};
    }

    /**
     * Tests the sentiment analysis for a query with only "sad" descriptions.
     *
     * <p>Verifies that all individual sentiments and the overall sentiment are correctly
     * identified as "sad".</p>
     *
     * @throws Exception if an unexpected error occurs during the test
     * @author Devang Shah
     */
    @Test
    public void testSentimentAnalysisSad() {
        new TestKit(system) {{
            SentimentAnalyser analyser = new SentimentAnalyser(
                    List.of("great", "awesome", "happy"),
                    List.of("sad", "bad", "terrible")
            );
            ActorRef sentimentActor = system.actorOf(SentimentActor.props(analyser, getRef()));

            List<String> descriptions = List.of(
                    "This is a bad experience.",
                    "Feeling very sad today.",
                    "What a terrible situation."
            );
            sentimentActor.tell(new SentimentActor.SentimentRequest("query2", descriptions), getRef());

            SentimentActor.SentimentResponse response = expectMsgClass(SentimentActor.SentimentResponse.class);

            assertEquals("query2", response.query);
            assertEquals(":-|", response.overallSentiment);
            assertEquals(3, response.sentiments.size());
            assertEquals(":-(", response.sentiments.get(0));
            assertEquals(":-(", response.sentiments.get(1));
            assertEquals(":-(", response.sentiments.get(2));
        }};
    }

    /**
     * Tests the sentiment analysis for a query with "neutral" descriptions.
     *
     * <p>Verifies that all individual sentiments and the overall sentiment are correctly
     * identified as "neutral".</p>
     *
     * @throws Exception if an unexpected error occurs during the test
     * @author Devang Shah
     */
    @Test
    public void testSentimentAnalysisNeutral() {
        new TestKit(system) {{
            SentimentAnalyser analyser = new SentimentAnalyser(
                    List.of("great", "awesome", "happy"),
                    List.of("sad", "bad", "terrible")
            );
            ActorRef sentimentActor = system.actorOf(SentimentActor.props(analyser, getRef()));

            List<String> descriptions = List.of(
                    "This is okay.",
                    "Nothing much to say here.",
                    "Neutral feelings about this."
            );
            sentimentActor.tell(new SentimentActor.SentimentRequest("query3", descriptions), getRef());

            SentimentActor.SentimentResponse response = expectMsgClass(SentimentActor.SentimentResponse.class);

            assertEquals("query3", response.query);
            assertEquals(":-|", response.overallSentiment);
            assertEquals(3, response.sentiments.size());
            assertEquals(":-|", response.sentiments.get(0));
            assertEquals(":-|", response.sentiments.get(1));
            assertEquals(":-|", response.sentiments.get(2));
        }};
    }

    /**
     * Tests the sentiment analysis for a query with no descriptions.
     *
     * <p>Verifies that the overall sentiment is identified as "neutral" and no individual
     * sentiments are returned.</p>
     *
     * @throws Exception if an unexpected error occurs during the test
     * @author Devang Shah
     */
    @Test
    public void testSentimentAnalysisNoDescriptions() {
        new TestKit(system) {{
            SentimentAnalyser analyser = new SentimentAnalyser(
                    List.of("great", "awesome", "happy"),
                    List.of("sad", "bad", "terrible")
            );
            ActorRef sentimentActor = system.actorOf(SentimentActor.props(analyser, getRef()));

            sentimentActor.tell(new SentimentActor.SentimentRequest("query4", List.of()), getRef());

            SentimentActor.SentimentResponse response = expectMsgClass(SentimentActor.SentimentResponse.class);

            assertEquals("query4", response.query);
            assertEquals("neutral", response.overallSentiment);
            assertEquals(0, response.sentiments.size());
        }};
    }
}