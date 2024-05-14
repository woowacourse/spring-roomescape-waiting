package roomescape.exception;

public class SignupFailException extends IllegalArgumentException {
    public SignupFailException(final String message) {
        super(message);
    }
}
