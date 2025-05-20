package roomescape.common.exception;

public class PastDateException extends RuntimeException {
    public PastDateException(String message) {
        super(message);
    }
}
