import com.google.gson.Gson;

import java.util.Date;

import static spark.Spark.*;

public class Main {
    public static class Video {
        double duration;
        long timestamp;
    }

    public static void main(String[] args) {
        VideoStatistics videoStatistics = new VideoStatistics(() -> new Date().getTime());

        post("/videos", (request, response) -> {
            Video video = new Gson().fromJson(request.body(), Video.class);
            try {
                synchronized (videoStatistics) {
                    videoStatistics.post(video.duration, video.timestamp);
                }
            } catch (IllegalArgumentException e) {
                response.status(204);
                return "";
            }

            response.status(201);
            return "";
        });

        delete("/videos", (request, response) -> {
            synchronized (videoStatistics) {
                videoStatistics.delete();
            }

            response.status(204);
            return "";
        });

        get("/statistics", (request, response) -> {
            VideoStatistics.Result result;
            synchronized (videoStatistics) {
                result = videoStatistics.get(new Date().getTime());
            }
            return new Gson().toJson(result);
        });
    }
}