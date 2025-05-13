package roomescape.common.exceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.common.exception.LoginException;
import roomescape.common.exceptionHandler.dto.ExceptionResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String EXCEPTION_PREFIX = "[ERROR] ";

    @ExceptionHandler(value = LoginException.class)
    public ResponseEntity<ExceptionResponse> loginFail(
            final LoginException exception, final HttpServletRequest request
    ) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                EXCEPTION_PREFIX + exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> invalidInput(
            final IllegalArgumentException exception, final HttpServletRequest request
    ) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                EXCEPTION_PREFIX + exception.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<ExceptionResponse> serverError(final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                EXCEPTION_PREFIX + "서버의 오류입니다. 관리자에게 문의해주세요.", request.getRequestURI());
        return ResponseEntity.internalServerError().body(exceptionResponse);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> notReadable(
            final HttpMessageNotReadableException exception, final HttpServletRequest request
    ) {
        Throwable rootCause = exception.getRootCause();
        if (rootCause instanceof IllegalArgumentException) {
            ExceptionResponse exceptionResponse = new ExceptionResponse(
                    EXCEPTION_PREFIX + rootCause.getMessage(), request.getRequestURI()
            );
            return ResponseEntity.badRequest().body(exceptionResponse);
        }

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                EXCEPTION_PREFIX + "요청 입력이 잘못되었습니다.", request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResponse> unknownException(final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                EXCEPTION_PREFIX + "예상치 못한 서버 오류입니다. 서버에 문의해주세요.", request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(exceptionResponse);
    }
}
