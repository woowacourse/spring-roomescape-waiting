package roomescape.application.support.exception;

public class JwtExtractException extends CoreException {

    public JwtExtractException(String message) {
        super(message);
    }

    public JwtExtractException(String message, Throwable cause) {
        super(message, cause);
    }
}
