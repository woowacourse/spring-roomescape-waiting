package roomescape.auth.web.controller.advice;

import static roomescape.auth.web.controller.response.AuthErrorCode.NOT_ADMIN;
import static roomescape.auth.web.controller.response.AuthErrorCode.NOT_AUTHORIZED;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.auth.web.exception.NotAdminException;
import roomescape.auth.web.exception.TokenNotFoundException;
import roomescape.global.response.ApiResponse;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNotAdminException(NotAdminException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(NOT_ADMIN, e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleTokenNotFoundException(TokenNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(NOT_AUTHORIZED, e.getMessage()));
    }
}
