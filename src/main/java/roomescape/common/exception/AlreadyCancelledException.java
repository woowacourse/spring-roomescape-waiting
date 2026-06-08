package roomescape.common.exception;

public class AlreadyCancelledException extends RuntimeException {
    public AlreadyCancelledException(String message) {
        super(message);
    }
}
