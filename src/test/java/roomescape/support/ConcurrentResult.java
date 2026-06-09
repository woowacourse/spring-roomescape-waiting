package roomescape.support;

public record ConcurrentResult(boolean success, Throwable exception) {


    public static ConcurrentResult withSuccess() {
        return new ConcurrentResult(true, null);
    }

    public static ConcurrentResult withFail(Throwable exception) {
        return new ConcurrentResult(false, exception);
    }
}
