package roomescape.exception.auth;

public class AuthTokenNotFoundException extends RuntimeException {

    public AuthTokenNotFoundException(String message) {
        super(message);
    }
}
