package roomescape.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(ReservationException.class)
    public ProblemDetail handleReservationException(ReservationException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ProblemDetail handleUnAuthorizedException(UnAuthorizedException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "인증 정보를 찾을 수 없습니다.");
    }

    @ExceptionHandler(UnAvailableReservationException.class)
    public ProblemDetail handleUnAvailableReservationException(UnAvailableReservationException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundReservationInfo(NotFoundException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DeletionNotAllowedException.class)
    public ProblemDetail handleDeletionNotAllowedException(DeletionNotAllowedException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.debug("NoResourceFoundException: ", e);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception e) {
        log.error("예상치 못한 예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
