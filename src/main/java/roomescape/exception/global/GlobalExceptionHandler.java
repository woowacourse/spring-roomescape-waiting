package roomescape.exception.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundMemberException;
import roomescape.exception.NotFoundReservationException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.exception.NotFoundThemeException;
import roomescape.exception.UnableCreateMemberException;
import roomescape.exception.UnableReservationException;
import roomescape.exception.UnauthorizedException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnableCreateMemberException.class)
    public ProblemDetail handleUnableCreateMemberException(UnableCreateMemberException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnAuthorizedException(UnauthorizedException e) {
        log.error("예외 발생: ", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "인증 정보를 찾을 수 없습니다.");
    }

    @ExceptionHandler(UnableReservationException.class)
    public ProblemDetail handleUnAvailableReservationException(UnableReservationException e) {
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
