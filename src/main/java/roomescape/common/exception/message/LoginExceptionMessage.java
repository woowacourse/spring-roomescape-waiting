package roomescape.common.exception.message;

public enum LoginExceptionMessage {
    INVALID_TOKEN("토큰이 존재하지 않습니다"),
    INVALID_TOKEN_VALUE("토큰 값이 존재하지 않습니다"),
    INVALID_COOKIE("쿠키가 존재하지 않습니다"),
    AUTHENTICATION_FAIL("로그인에 실패했습니다");

    private final String message;

    LoginExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
