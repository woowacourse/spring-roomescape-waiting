package roomescape.global;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.ErrorResponse;
import roomescape.global.exception.RoomescapeException;
import roomescape.payment.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_REQUEST);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_DATE_FORMAT);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RoomescapeException.class)
    protected ResponseEntity<ErrorResponse> handleRoomescapeException(RoomescapeException ex) {
        ErrorCode error = ex.getErrorCode();

        return ResponseEntity.status(error.getStatus()).body(ErrorResponse.of(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_REQUEST);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(TossPaymentException.class)
    protected ResponseEntity<ErrorResponse> handleTossPaymentException(TossPaymentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("TOSS_PAYMENT_ERROR", ex.getMessage()));
    }
}
