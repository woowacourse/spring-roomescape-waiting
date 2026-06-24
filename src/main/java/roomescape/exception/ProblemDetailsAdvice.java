package roomescape.exception;

import jakarta.validation.ConstraintViolationException;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.gateway.toss.TossPaymentException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProblemDetailsAdvice {

    private final Map<Class<? extends RoomescapeException>, HttpStatus> exceptionHttpStatusMap = new ConcurrentHashMap<>();

    public ProblemDetailsAdvice() {
        exceptionHttpStatusMap.put(DuplicateReservationException.class, HttpStatus.CONFLICT);
        exceptionHttpStatusMap.put(DuplicateSessionException.class, HttpStatus.CONFLICT);
        exceptionHttpStatusMap.put(DuplicateTimeException.class, HttpStatus.CONFLICT);
        exceptionHttpStatusMap.put(DuplicateWaitingException.class, HttpStatus.CONFLICT);
        exceptionHttpStatusMap.put(InvalidOwnershipException.class, HttpStatus.FORBIDDEN);
        exceptionHttpStatusMap.put(InvalidWaitingPrerequisiteException.class, HttpStatus.BAD_REQUEST);
        exceptionHttpStatusMap.put(PastSessionControlException.class, HttpStatus.BAD_REQUEST);
        exceptionHttpStatusMap.put(PastTimeException.class, HttpStatus.BAD_REQUEST);
        exceptionHttpStatusMap.put(ReservationNotFoundException.class, HttpStatus.NOT_FOUND);
        exceptionHttpStatusMap.put(SessionNotFoundException.class, HttpStatus.NOT_FOUND);
        exceptionHttpStatusMap.put(ResourceInUseException.class, HttpStatus.CONFLICT);
        exceptionHttpStatusMap.put(ThemeNotFoundException.class, HttpStatus.NOT_FOUND);
        exceptionHttpStatusMap.put(TimeSlotNotFoundException.class, HttpStatus.NOT_FOUND);
        exceptionHttpStatusMap.put(UnauthorizedException.class, HttpStatus.UNAUTHORIZED);
        exceptionHttpStatusMap.put(WaitingNotFoundException.class, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(RoomescapeException exception) {
        HttpStatus status = exceptionHttpStatusMap.getOrDefault(exception.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problemDetail.setProperty("code", exception.getErrorCode());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ResponseEntity<ProblemDetail> handlePaymentAmountMismatchException(PaymentAmountMismatchException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setProperty("code", "PAYMENT_AMOUNT_MISMATCH");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ProblemDetail> handleTossPaymentException(TossPaymentException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        problemDetail.setProperty("code", exception.getCode());
        return ResponseEntity.status(exception.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setProperty("code", "INVALID_DOMAIN_STATE");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateKeyException(DuplicateKeyException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다.");
        problemDetail.setProperty("code", "DUPLICATE_KEY_VIOLATION");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "데이터 무결성 제약조건이 위반되었습니다.");
        problemDetail.setProperty("code", "DATA_INTEGRITY_VIOLATION");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "파라미터 값이 유효하지 않습니다.");
        problemDetail.setProperty("code", "INVALID_PARAMETER");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다.");
        problemDetail.setProperty("code", "INVALID_REQUEST_BODY");
        problemDetail.setProperty("errors", extractErrors(exception));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "요청 본문의 형식이 잘못되었습니다.");
        problemDetail.setProperty("code", "BAD_REQUEST_FORMAT");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    private List<Map<String, String>> extractErrors(MethodArgumentNotValidException exception) {
        return exception.getBindingResult().getFieldErrors().stream()
                .map(this::toErrorMap)
                .toList();
    }

    private Map<String, String> toErrorMap(FieldError error) {
        return Map.of(
                "field", error.getField(),
                "message", error.getDefaultMessage()
        );
    }
}
