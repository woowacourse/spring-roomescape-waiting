package roomescape.global.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.auth.exception.AuthenticationException;
import roomescape.auth.exception.AuthorizationException;
import roomescape.global.exception.BusinessException;
import roomescape.global.exception.DeleteFailedException;
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(new ErrorResponse("서버 내부 예외가 발생했습니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("필수 요청 파라미터가 누락되었습니다."));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return makeResponse(e, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponse> makeResponse(
            BusinessException e,
            HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(e));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatchException(
            MissingServletRequestParameterException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("요청 파라미터 형식이 유효하지 않습니다."));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateException e) {
        return makeResponse(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DeleteFailedException.class)
    public ResponseEntity<ErrorResponse> handleDeleteFailedException(DeleteFailedException e) {
        return makeResponse(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidRequestValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestValueException(InvalidRequestValueException e) {
        return makeResponse(e, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        return makeResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        return makeResponse(e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(AuthorizationException e) {
        return makeResponse(e, HttpStatus.FORBIDDEN);
    }
}
