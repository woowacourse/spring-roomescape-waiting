package roomescape.exception;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReservationAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handle(ReservationAlreadyExistException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_RESERVATION", e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("RESERVATION_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(ReservationTimeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ReservationTimeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("TIME_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(ThemeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ThemeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("THEME_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(ExpiredDateTimeException.class)
    public ResponseEntity<ErrorResponse> handle(ExpiredDateTimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_DATE_OR_TIME", e.getMessage()));
    }

    @ExceptionHandler(ReferencedDataException.class)
    public ResponseEntity<ErrorResponse> handle(ReferencedDataException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("DIFFERENCE_DATA_EXISTS", e.getMessage()));
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidInputException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_INPUT", e.getMessage()));
    }

    @ExceptionHandler({ConcurrencyConflictException.class, ConcurrencyFailureException.class})
    public ResponseEntity<ErrorResponse> handleConcurrencyConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("LOCK_CONFLICT", "일시적 충돌이 발생했습니다. 다시 시도해주세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handle(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DATA_INTEGRITY_VIOLATION", "데이터 충돌이 발생했습니다. 다시 시도해주세요."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handle(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNER_SERVER_ERROR", "서버에 오류가 발생하였습니다. 다시 시도해주세요."));
    }
}
