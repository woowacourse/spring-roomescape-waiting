package roomescape.exception.auth;

public class AuthenticationException extends IllegalArgumentException {

    public AuthenticationException(String message) {
        super(message);
    }
}
