package roomescape.common.handler;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.common.exception.BadRequestException;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.infrastructure.payment.toss.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<ErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(ErrorDetail::from)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.of("입력값 이상", fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from("요청 JSON 형식이 잘못되었습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<GlobalErrorResponse> handleBadRequestException(BadRequestException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<GlobalErrorResponse> handleForbiddenException(ForbiddenException e) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleNotFoundException(NotFoundException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<GlobalErrorResponse> handleConflictException(ConflictException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<GlobalErrorResponse> handleUnprocessableEntityException(UnprocessableEntityException e) {

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<GlobalErrorResponse> handleNullPointerException(NullPointerException e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ResponseEntity<GlobalErrorResponse> handlePaymentAmountMismatchException(PaymentAmountMismatchException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<GlobalErrorResponse> handleTossPaymentException(TossPaymentException e) {
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }
}
