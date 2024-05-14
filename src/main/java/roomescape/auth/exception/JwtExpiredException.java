package roomescape.auth.exception;

public class JwtExpiredException extends AuthenticationException {
    private static final String ERROR_MESSAGE = "해당 토큰이 만료되었습니다.";

    public JwtExpiredException() {
        super(ERROR_MESSAGE);
    }
}
