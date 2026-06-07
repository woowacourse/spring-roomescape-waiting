package roomescape.infra.queue;

public record JobResult<R>(String status, R data, String errorMessage) {

    public static <R> JobResult<R> pending() {
        return new JobResult<>("PENDING", null, null);
    }

    public static <R> JobResult<R> success(R data) {
        return new JobResult<>("SUCCESS", data, null);
    }

    public static <R> JobResult<R> failed(String errorMessage) {
        return new JobResult<>("FAILED", null, errorMessage);
    }
}
