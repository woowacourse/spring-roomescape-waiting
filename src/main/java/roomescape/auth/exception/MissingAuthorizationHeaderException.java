package roomescape.auth.exception;

public class MissingAuthorizationHeaderException extends AuthenticationException {

    public MissingAuthorizationHeaderException() {
        super("Authorization 헤더가 필요합니다.");
    }
}
