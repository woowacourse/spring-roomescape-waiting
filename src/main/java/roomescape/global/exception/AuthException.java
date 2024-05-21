package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {

    private final ErrorResponse errorResponse;

    public AuthException(final AuthErrorType authErrorType) {
        super(authErrorType.getErrorMessage());
        this.errorResponse = new ErrorResponse(
                authErrorType.getHttpStatus(),
                authErrorType.getErrorMessage()
        );
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public enum AuthErrorType {
        ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

        private final HttpStatus httpStatus;
        private final String errorMessage;

        AuthErrorType(final HttpStatus httpStatus, final String errorMessage) {
            this.httpStatus = httpStatus;
            this.errorMessage = errorMessage;
        }

        public HttpStatus getHttpStatus() {
            return httpStatus;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
