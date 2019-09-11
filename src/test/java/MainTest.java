import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.servlet.SparkApplication;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest  {
    public static class TestSparkApplication implements SparkApplication {
        @Override
        public void init() {
            String[] args = {};
            Main.main(args);
        }
    }

    public static class TestSparkServer extends SparkServer<TestSparkApplication> {
        public TestSparkServer(Class<TestSparkApplication> sparkApplicationClass, int port) {
            super(sparkApplicationClass, port);
        }

        public void beforeTests() throws Throwable { before(); }

        public void afterTests() { after(); }
    }

    public static TestSparkServer testServer = new
            TestSparkServer(TestSparkApplication.class, 4567);

    @BeforeAll
    public static void setup() throws Throwable {
        testServer.beforeTests();
    }

    @AfterAll
    public static void tearDown() {
        testServer.afterTests();
    }

    @Test
    public void testOldTimestamp() throws HttpClientException, InterruptedException {
        Main.Video video = new Main.Video();
        video.duration = 10.5;
        video.timestamp = new Date().getTime() - 61000;
        PostMethod request = testServer.post(
                "/videos", new Gson().toJson(video), false
        );

        HttpResponse response = testServer.execute(request);
        assertEquals(response.code(), 204);
    }

    @Test
    public void testPostAndStatistics() throws HttpClientException {
        Main.Video video = new Main.Video();
        video.timestamp = new Date().getTime();

        video.duration = 10;
        PostMethod request = testServer.post(
                "/videos", new Gson().toJson(video), false
        );
        HttpResponse response = testServer.execute(request);
        assertEquals(response.code(), 201);

        video.duration = 20;
        request = testServer.post(
                "/videos", new Gson().toJson(video), false
        );
        response = testServer.execute(request);

        GetMethod statsRequest = testServer.get("/statistics", false);
        response = testServer.execute(statsRequest);

        VideoStatistics.Result result = new Gson().fromJson(new String(response.body()), VideoStatistics.Result.class);
        assertEquals(result.sum, 30);
        assertEquals(result.count, 2);
        assertEquals(result.avg, 15);
        assertEquals(result.min, 10);
        assertEquals(result.max, 20);
    }

    @Test
    public void testDelete() throws HttpClientException {
        Main.Video video = new Main.Video();
        video.duration = 10.5;
        video.timestamp = new Date().getTime();
        PostMethod request = testServer.post(
                "/videos", new Gson().toJson(video), false
        );

        HttpResponse response = testServer.execute(request);
        assertEquals(response.code(), 201);
        DeleteMethod deleteRequest = testServer.delete("/videos", false);
        response = testServer.execute(deleteRequest);
        assertEquals(response.code(), 204);

        GetMethod statsRequest = testServer.get("/statistics", false);
        response = testServer.execute(statsRequest);

        VideoStatistics.Result result = new Gson().fromJson(new String(response.body()), VideoStatistics.Result.class);
        assertEquals(result.count, 0);
    }
}
