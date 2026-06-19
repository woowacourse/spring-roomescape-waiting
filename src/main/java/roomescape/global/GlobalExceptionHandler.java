package roomescape.global;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.exception.ErrorCode;
import roomescape.exception.ErrorResponse;
import roomescape.exception.RoomescapeException;
import roomescape.payment.PaymentAmountMismatchException;

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

    @ExceptionHandler(PaymentAmountMismatchException.class)
    protected ResponseEntity<ErrorResponse> handlePaymentAmountMismatchException(PaymentAmountMismatchException ex) {
        ErrorResponse response = new ErrorResponse("PAYMENT_AMOUNT_MISMATCH", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
