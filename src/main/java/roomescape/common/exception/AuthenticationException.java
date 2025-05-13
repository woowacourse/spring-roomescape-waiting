package roomescape.common.exception;

public class AuthenticationException extends CustomException {

    public AuthenticationException(String message) {
        super(message, ErrorCode.UNAUTHORIZED);
    }

    public AuthenticationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
