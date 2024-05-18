package roomescape.exception;

public class BadArgumentRequestException extends RuntimeException {
    public BadArgumentRequestException(String message) {
        super(message);
    }
}
