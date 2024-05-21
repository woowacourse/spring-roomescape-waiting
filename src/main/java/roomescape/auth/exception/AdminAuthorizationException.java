package roomescape.auth.exception;

public class AdminAuthorizationException extends RuntimeException {
    private static final String ERROR_MESSAGE = "관리자만 접근할 수 있습니다.";

    public AdminAuthorizationException() {
        super(ERROR_MESSAGE);
    }
}
