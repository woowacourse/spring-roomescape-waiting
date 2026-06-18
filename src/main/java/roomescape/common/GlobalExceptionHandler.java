package roomescape.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.common.exception.ExceptionType;
import roomescape.domain.RoomEscapeException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public static final String UNEXPECTED_ERROR = "예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    public static final String DATABASE_ERROR = "데이터 베이스 관련 오류가 발생했습니다. 관리자에게 문의해주세요";
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RoomEscapeException.class)
    public ProblemDetail roomEscapeExceptionHandle(RoomEscapeException e) {
        log.info("도메인 관련 오류가 발생했습니다.", e);
        return ProblemDetail.forStatusAndDetail(ExceptionType.resolveStatus(e.code()), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail methodArgumentNotValidExceptionHandle(MethodArgumentNotValidException e) {
        log.info("입력 값 검증 중 예외가 발생했습니다.", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParams(MissingServletRequestParameterException e) {
        log.info("입력 값 검증 중 예외가 발생했습니다.", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "필수 파라미터가 없습니다.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail httpRequestMethodNotSupportedExceptionHandle(HttpRequestMethodNotSupportedException e) {
        log.info("지원하지 않는 HTTP 메서드입니다.", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ProblemDetail invalidDataAccessApiUsageExceptionHandle(InvalidDataAccessApiUsageException e) {
        if (e.getCause() instanceof RoomEscapeException roomEscapeException) {
            return roomEscapeExceptionHandle(roomEscapeException);
        }
        log.error("데이터베이스 관련 오류가 발생했습니다", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, DATABASE_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail dataIntegrityViolationExceptionHandle(DataIntegrityViolationException e) {
        log.info("데이터 무결성 위반 오류가 발생했습니다.", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "이미 존재하는 예약입니다.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail dataAccessExceptionHandle(DataAccessException e) {
        log.error("데이터베이스 관련 오류가 발생했습니다", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, DATABASE_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail exceptionHandle(Exception e) {
        log.error("서버 내부 오류입니다.", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR);
    }
}
