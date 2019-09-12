import com.google.gson.Gson;

import java.util.Date;

import static spark.Spark.*;

public class Main {
    public static class Video {
        double duration;
        long timestamp;
    }

    public static void main(String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        threadPool(2 * cores);

        VideoStatistics videoStatistics = new VideoStatistics(() -> new Date().getTime());

        post("/videos", (request, response) -> {
            Video video = new Gson().fromJson(request.body(), Video.class);
            try {
                videoStatistics.post(video.duration, video.timestamp);
            } catch (IllegalArgumentException e) {
                response.status(204);
                return "";
            }

            response.status(201);
            return "";
        });

        delete("/videos", (request, response) -> {
            videoStatistics.delete();

            response.status(204);
            return "";
        });

        get("/statistics", (request, response) -> {
            VideoStatistics.Result result;
            result = videoStatistics.get(new Date().getTime());
            return new Gson().toJson(result);
        });
    }
}