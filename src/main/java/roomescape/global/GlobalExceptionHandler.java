package roomescape.global;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.reservation.application.exception.BadRequestException;
import roomescape.reservation.application.exception.ConflictException;

@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionHandler {

    private static final Map<Class<?>, String> ERROR_MESSAGES = Map.of(
            LocalTime.class, "[ERROR] 시간 형식은 HH:mm 이어야 합니다.",
            LocalDate.class, "[ERROR] 날짜 형식은 yyyy-MM-dd 이어야 합니다."
    );

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleRoomEscape(RoomEscapeException e) {
        return ResponseEntity
                .status(e.errorCode().status())
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("참조 중인 데이터가 존재하여 삭제할 수 없습니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();

        return createBadRequestResponse(errorMessage);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        String errorMessage = resolveErrorMessage(e);

        return createBadRequestResponse(errorMessage);
    }

    private String resolveErrorMessage(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
            return ERROR_MESSAGES.getOrDefault(invalidFormatException.getTargetType(), e.getMessage());
        }
        return e.getMessage();
    }

    private ResponseEntity<ErrorResponse> createBadRequestResponse(String errorMessage) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage));
    }

    public record ErrorResponse(String errorMessage) {
    }
}
