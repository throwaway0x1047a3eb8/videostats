import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VideoStatisticsTest {
    private static final double DELTA = 0.00001;

    @Test
    public void testEmpty() {
        VideoStatistics.DateService dateService = () -> 61000;
        VideoStatistics stats = new VideoStatistics(dateService);
        VideoStatistics.Result result = stats.get();
        assertEquals(result.count, 0);
        assertEquals(result.sum, 0, DELTA);
        assertEquals(result.avg, 0, DELTA);
        assertEquals(result.max, 0, DELTA);
        assertEquals(result.min, 0, DELTA);
    }

    @Test
    public void testOldTimestamp() {
        VideoStatistics.DateService dateService = () -> 61000;

        VideoStatistics stats = new VideoStatistics(dateService);

        assertThrows(IllegalArgumentException.class, () -> {
            stats.post(3, 0);
        });
    }

    @Test
    public void testSinglePost() {
        VideoStatistics.DateService dateService = () -> 61000;
        VideoStatistics stats = new VideoStatistics(dateService);

        stats.post(10.5, 60666);

        VideoStatistics.Result result = stats.get();
        assertEquals(result.count, 1);
        assertEquals(result.sum, 10.5, DELTA);
        assertEquals(result.avg, 10.5, DELTA);
        assertEquals(result.max, 10.5, DELTA);
        assertEquals(result.min, 10.5, DELTA);
    }

    @Test
    public void testOldEntry() {
        Queue<Long> timeResults = new LinkedList<>();

        // getTime called for post.
        timeResults.add(61000L);

        // getTime called for get
        timeResults.add(121000L);

        VideoStatistics.DateService dateService = timeResults::remove;
        VideoStatistics stats = new VideoStatistics(dateService);

        stats.post(10.5, 60666);


        VideoStatistics.Result result = stats.get();
        assertEquals(result.count, 0);
        assertEquals(result.sum, 0, DELTA);
        assertEquals(result.avg, 0, DELTA);
        assertEquals(result.max, 0, DELTA);
        assertEquals(result.min, 0, DELTA);
    }

    @Test
    public void testMultiplePosts() {
        VideoStatistics.DateService dateService = () -> 61000;
        VideoStatistics stats = new VideoStatistics(dateService);

        stats.post(5, 60666);
        stats.post(25, 60667);
        stats.post(125, 60668);

        VideoStatistics.Result result = stats.get();
        assertEquals(result.count, 3);
        assertEquals(result.sum, 155, DELTA);
        assertEquals(result.avg, 51.666666666, DELTA);
        assertEquals(result.max, 125, DELTA);
        assertEquals(result.min, 5, DELTA);
    }

    @Test
    public void testMinMax() {
        VideoStatistics.DateService dateService = () -> 61000;
        VideoStatistics stats = new VideoStatistics(dateService);

        stats.post(5, 60666);
        stats.post(1, 60667);
        stats.post(25, 60668);
        stats.post(625, 60669);
        stats.post(125, 60670);

        VideoStatistics.Result result = stats.get();
        // Ensures min/max values are updated accordingly.
        assertEquals(result.max, 625, DELTA);
        assertEquals(result.min, 1, DELTA);
    }

    @Test
    public void testCollision() {
        Queue<Long> timeResults = new LinkedList<>();

        // getTime called for the first and second post.
        timeResults.add(61000L);
        timeResults.add(61000L);

        // getTime called for the get call.
        timeResults.add(121000L);

        VideoStatistics.DateService dateService = timeResults::remove;
        VideoStatistics stats = new VideoStatistics(dateService);

        stats.post(5, 60666);
        // Happens 1 minute after the previous post call.
        // Collides with it and must replace it.
        stats.post(25, 120666);

        VideoStatistics.Result result = stats.get();
        assertEquals(result.count, 1);
        assertEquals(result.sum, 25, DELTA);
        assertEquals(result.avg, 25, DELTA);
        assertEquals(result.max, 25, DELTA);
        assertEquals(result.min, 25, DELTA);
    }

    @Test
    public void testDelete() {
        VideoStatistics.DateService dateService = () -> 61000;
        VideoStatistics stats = new VideoStatistics(dateService);

        stats.post(5, 60666);
        stats.post(25, 60667);
        stats.post(125, 60668);

        // None of the post calls above should matter.
        stats.delete();

        VideoStatistics.Result result = stats.get();
        assertEquals(result.count, 0);
        assertEquals(result.sum, 0, DELTA);
        assertEquals(result.avg, 0, DELTA);
        assertEquals(result.max, 0, DELTA);
        assertEquals(result.min, 0, DELTA);
    }
}
