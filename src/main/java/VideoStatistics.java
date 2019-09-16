public class VideoStatistics {
    private static class Second {
        double sum;
        long count;
        double max;
        double min;

        int timestamp;
    }

    public static class Result {
        double sum;
        double avg;
        long count;
        double max;
        double min;
    }

    public interface DateService {
        long getTime();
    }

    private Second[] seconds;

    private DateService dateService;

    public VideoStatistics(DateService dateService) {
        this.dateService = dateService;

        seconds = new Second[60];
        for (int i = 0; i < 60; i++) {
            seconds[i] = new Second();
            seconds[i].max = 0;
            seconds[i].min = Double.MAX_VALUE;
        }
    }

    public void post(double duration, long timestamp) throws IllegalArgumentException {
        int timestampSeconds = (int) (timestamp / 1000);
        int nowSeconds = (int) (dateService.getTime() / 1000);

        if (nowSeconds - timestampSeconds > 60) {
            throw new IllegalArgumentException("Timestamp older than 60 seconds.");
        }

        synchronized (seconds[timestampSeconds % 60]) {
            Second second = seconds[timestampSeconds % 60];
            if (second.timestamp != timestampSeconds) {
                second.sum = 0;
                second.count = 0;
                second.max = 0;
                second.min = Double.MAX_VALUE;
            }

            second.timestamp = timestampSeconds;
            second.sum += duration;
            second.count += 1;
            second.max = Math.max(second.max, duration);
            second.min = Math.min(second.min, duration);
        }
    }

    public void delete() {
        synchronized (this) {
            for (int i = 0; i < 60; i++) {
                synchronized (seconds[i]) {
                    Second second = seconds[i];
                    second.sum = 0;
                    second.count = 0;
                    second.max = 0;
                    second.min = Double.MAX_VALUE;
                }
            }
        }
    }

    public Result get() {
        int nowSeconds = (int) (dateService.getTime() / 1000);

        Result statistics = new Result();
        statistics.max = 0;
        statistics.min = Double.MAX_VALUE;

        synchronized (this) {
            for (int i = 0; i < 60; i++) {
                synchronized (seconds[i]) {
                    Second second = seconds[i];

                    if (nowSeconds - second.timestamp > 60) continue;

                    statistics.sum += second.sum;
                    statistics.count += second.count;
                    statistics.max = Math.max(statistics.max, second.max);
                    statistics.min = Math.min(statistics.min, second.min);
                }
            }
        }

        statistics.avg = statistics.count == 0 ? 0 : statistics.sum / statistics.count;
        statistics.min = statistics.min == Double.MAX_VALUE ? 0 : statistics.min;

        return statistics;

    }
}
