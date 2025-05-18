package roomescape.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

    @ExceptionHandler(NotFoundReservationException.class)
    public ProblemDetail handleNotFoundReservationInfo(NotFoundReservationException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "입력하신 예약 정보를 찾지 못했습니다.");
    }

    @ExceptionHandler(NotFoundMemberException.class)
    public ProblemDetail handleNotFoundMemberException(NotFoundMemberException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "입력하신 사용자 정보를 찾지 못했습니다.");
    }

    @ExceptionHandler(NotFoundThemeException.class)
    public ProblemDetail handleNotFoundThemeException(NotFoundThemeException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "입력하신 테마 정보를 찾지 못했습니다.");
    }

    @ExceptionHandler(NotFoundReservationTimeException.class)
    public ProblemDetail handleNotFoundReservationTimeException(NotFoundReservationTimeException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "입력하신 예약 시간 정보를 찾지 못했습니다.");
    }

    @ExceptionHandler(DeletionNotAllowedException.class)
    public ProblemDetail handleDeletionNotAllowedException(DeletionNotAllowedException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
