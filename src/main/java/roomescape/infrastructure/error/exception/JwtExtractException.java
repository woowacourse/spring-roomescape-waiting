package roomescape.infrastructure.error.exception;

public class JwtExtractException extends RuntimeException {

    public JwtExtractException(String message) {
        super(message);
    }

    public JwtExtractException(String message, Throwable cause) {
        super(message, cause);
    }
}
