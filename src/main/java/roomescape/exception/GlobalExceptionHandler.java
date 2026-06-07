package roomescape.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.exception.custom.AlreadyReservedException;
import roomescape.exception.custom.AlreadyWaitingException;
import roomescape.exception.custom.CannotCreatePastReservationException;
import roomescape.exception.custom.CannotDeleteReservationTimeInUseException;
import roomescape.exception.custom.CannotDeleteThemeInUseException;
import roomescape.exception.custom.CannotModifyPastReservationException;
import roomescape.exception.custom.InvalidDomainValueException;
import roomescape.exception.custom.InvalidRequestArgumentException;
import roomescape.exception.custom.RankingPeriodEndDateBeforeStartDateException;
import roomescape.exception.custom.RankingPeriodExceedsLimitException;
import roomescape.exception.custom.RankingPeriodPastDateOnlyException;
import roomescape.exception.custom.ReservationNotExistsException;
import roomescape.exception.custom.ReservationTimeAlreadyExistsException;
import roomescape.exception.custom.ReservationTimeNotExistsException;
import roomescape.exception.custom.ThemeNotExistsException;
import roomescape.exception.custom.WaitIsFullException;
import roomescape.exception.custom.WaitNotExistsException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int MAX_RANKING_PERIOD = 366;

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception) {
        log.info("[Invalid API Access Error]", exception);

        return new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), HttpStatus.METHOD_NOT_ALLOWED.name(),
                "지원하지 않는 메서드입니다.");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ErrorResponse handleNoResourceFoundException(
            NoResourceFoundException exception) {
        log.info("[Invalid API Access Error]", exception);

        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(),
                "잘못된 경로입니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "요청 본문(JSON)의 형식이 올바르지 않거나 읽을 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "요청 파라미터 또는 경로 변수의 타입이 올바르지 않습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationExceptionException(DataIntegrityViolationException exception) {
        log.info("[Data Integrity Violation Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.METHOD_NOT_ALLOWED.name(),
                "이미 존재하는 데이터이거나 유효하지 않은 값이 포함되어 있습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidRequestArgumentException.class)
    public ErrorResponse handleInvalidRequestException(InvalidRequestArgumentException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CannotCreatePastReservationException.class)
    public ErrorResponse handleCannotCreatePastReservationException(CannotCreatePastReservationException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "지나간 시간의 예약은 생성할 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CannotDeleteReservationTimeInUseException.class)
    public ErrorResponse handleCannotDeleteReservationTimeInUseException(
            CannotDeleteReservationTimeInUseException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "예약/대기에서 사용 중인 예약 시간은 삭제할 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CannotDeleteThemeInUseException.class)
    public ErrorResponse handleCannotDeleteThemeInUseException(CannotDeleteThemeInUseException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "예약/대기에서 사용 중인 테마는 삭제할 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CannotModifyPastReservationException.class)
    public ErrorResponse handleCannotModifyPastReservationException(CannotModifyPastReservationException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "지나간 시간의 예약은 수정, 삭제할 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WaitIsFullException.class)
    public ErrorResponse handleWaitIsFullExceptionException(WaitIsFullException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "해당 슬롯에 대기 인원이 마감되었습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RankingPeriodEndDateBeforeStartDateException.class)
    public ErrorResponse handleRankingPeriodEndDateBeforeStartDateException(
            RankingPeriodEndDateBeforeStartDateException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "랭킹 조회 기간의 종료 날짜는 시작 날짜보다 빠를 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RankingPeriodExceedsLimitException.class)
    public ErrorResponse handleRankingPeriodExceedsLimitException(RankingPeriodExceedsLimitException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                String.format("랭킹 조회 기간이 최대 기간(%s일)을 초과했습니다.", MAX_RANKING_PERIOD));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RankingPeriodPastDateOnlyException.class)
    public ErrorResponse handleRankingPeriodPastDateOnlyException(RankingPeriodPastDateOnlyException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "랭킹 조회는 오늘 날짜 이전까지만 가능합니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AlreadyReservedException.class)
    public ErrorResponse handleAlreadyReservedExceptionExistsException(AlreadyReservedException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "이미 해당 슬롯에 예약을 신청했습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AlreadyWaitingException.class)
    public ErrorResponse handleAlreadyWaitingExceptionException(AlreadyWaitingException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "이미 해당 슬롯에 예약 대기를 신청했습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReservationTimeAlreadyExistsException.class)
    public ErrorResponse handleReservationTimeAlreadyExistsException(ReservationTimeAlreadyExistsException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "이미 중복된 예약 시간이 존재합니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReservationNotExistsException.class)
    public ErrorResponse handleReservationNotExistsException(ReservationNotExistsException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "해당 예약이 존재하지 않습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WaitNotExistsException.class)
    public ErrorResponse handleWaitNotExistsExceptionException(WaitNotExistsException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "해당 대기가 존재하지 않습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReservationTimeNotExistsException.class)
    public ErrorResponse handleReservationTimeNotExistsException(ReservationTimeNotExistsException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "해당 예약 시간이 존재하지 않습니다.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ThemeNotExistsException.class)
    public ErrorResponse handleThemeNotExistsException(ThemeNotExistsException exception) {
        log.info("[Invalid Request Error]", exception);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                "해당 테마가 존재하지 않습니다.");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InvalidDomainValueException.class)
    public ErrorResponse handleDomainValidException(InvalidDomainValueException exception) {
        log.error("[Domain Valid Error]", exception);

        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "서버 내부에서 에러가 발생했습니다.");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleOtherException(Exception exception) {
        log.error("[Internal Server Error]", exception);

        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "서버 내부에서 에러가 발생했습니다.");
    }
}
