package roomescape.advice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import roomescape.advice.dto.ErrorResponse;
import roomescape.auth.exception.AdminAuthorizationException;
import roomescape.auth.exception.AuthenticationException;
import roomescape.auth.exception.NotLoginAuthenticationException;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @DisplayName("IllegalArgumentException가 던져지면 400 에러를 반환한다.")
    @Test
    void handleIllegalArgumentExceptionTest() {
        IllegalArgumentException exception = new IllegalArgumentException("예외 메시지");
        ResponseEntity<ErrorResponse> expected = ResponseEntity.badRequest()
                .body(new ErrorResponse("예외 메시지"));

        ResponseEntity<ErrorResponse> actual = globalExceptionHandler.handleIllegalArgumentException(exception);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("NullPointerException이 던져지면 400 에러를 반환한다.")
    @Test
    void handleNullPointerExceptionTest() {
        NullPointerException exception = new NullPointerException();
        ResponseEntity<ErrorResponse> expected = ResponseEntity.badRequest()
                .body(new ErrorResponse("인자 중 null 값이 존재합니다."));

        ResponseEntity<ErrorResponse> actual = globalExceptionHandler.handleNullPointerException(exception);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("인증 에러가 발생하면 401 에러를 반환한다.")
    @Test
    void handleAuthenticationExceptionTest() {
        AuthenticationException exception = new NotLoginAuthenticationException();
        ResponseEntity<ErrorResponse> expected = ResponseEntity.status(401)
                .body(new ErrorResponse(exception.getMessage()));

        ResponseEntity<ErrorResponse> actual = globalExceptionHandler.handleAuthenticationException(exception);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("인증 에러가 발생하면 401 에러를 반환한다.")
    @Test
    void handleAdminAuthorizationExceptionTest() {
        AdminAuthorizationException exception = new AdminAuthorizationException();
        ResponseEntity<ErrorResponse> expected = ResponseEntity.status(403)
                .body(new ErrorResponse(exception.getMessage()));

        ResponseEntity<ErrorResponse> actual = globalExceptionHandler.handleAdminAuthorizationException(exception);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예상치 못한 에러가 발생하면 500 에러를 반환한다.")
    @Test
    void handleUnexpectedExceptionTest() {
        RuntimeException exception = new ArithmeticException();
        ResponseEntity<ErrorResponse> expected = ResponseEntity.internalServerError()
                .body(new ErrorResponse("예상치 못한 예외가 발생했습니다. 관리자에게 문의하세요."));

        ResponseEntity<ErrorResponse> actual = globalExceptionHandler.handleUnexpectedException(exception);

        assertThat(actual).isEqualTo(expected);
    }
}
