package roomescape.controller;

import common.exception.ErrorResponse;
import common.exception.RoomEscapeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    public static final String UNEXPECTED_ERROR = "예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    public static final String DATABASE_ERROR = "데이터 베이스 관련 오류가 발생했습니다. 관리자에게 문의해주세요";

    @ExceptionHandler(RoomEscapeException.class)
    public ResponseEntity<ErrorResponse> roomEscapeExceptionHandle(RoomEscapeException e) {
        log.info("도메인 관련 오류가 발생했습니다.", e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.create(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse methodArgumentNotValidExceptionHandle(MethodArgumentNotValidException e) {
        log.info("입력 값 검증 중 예외가 발생했습니다.", e);
        return ErrorResponse.create(e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse httpRequestMethodNotSupportedExceptionHandle(HttpRequestMethodNotSupportedException e) {
        log.info("지원하지 않는 HTTP 메서드입니다.", e);
        return ErrorResponse.create(e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse dataAccessExceptionHandle(DataAccessException e) {
        log.error("데이터베이스 관련 오류가 발생했습니다", e);
        return ErrorResponse.create(DATABASE_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse exceptionHandle(Exception e) {
        log.error("서버 내부 오류입니다.", e);
        return ErrorResponse.create(UNEXPECTED_ERROR);
    }
}
