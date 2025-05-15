package roomescape.common.exception;

public class AuthorizationException extends CustomException {

    public AuthorizationException() {
        super("권한이 부족합니다.", ErrorCode.FORBIDDEN);
    }

    public AuthorizationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
