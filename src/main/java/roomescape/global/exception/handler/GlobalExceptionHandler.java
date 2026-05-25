package roomescape.global.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.exception.BusinessException;
import roomescape.global.exception.DeleteFailedException;
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.response.ErrorResponse;
import roomescape.reservation.exception.ForbiddenException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(new ErrorResponse("서버 내부 예외가 발생했습니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("필수 요청 파라미터가 누락되었습니다."));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (e instanceof DuplicateException || e instanceof DeleteFailedException) {
            status = HttpStatus.CONFLICT;
        }
        if (e instanceof InvalidRequestValueException) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        if (e instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        if (e instanceof ForbiddenException) {
            status = HttpStatus.FORBIDDEN;
        }

        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(e));
    }
}
