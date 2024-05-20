package roomescape.global.exception;

public class IllegalRequestException extends RuntimeException {

    public IllegalRequestException(String message) {
        super(message);
    }

    public IllegalRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
