package roomescape.common.exception;

import static roomescape.common.exception.GlobalErrorCode.VALIDATION_ERROR;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Spring Mvc에서 던지는 4xx번대 예외들을 처리
 */
@RestControllerAdvice
@Order(0)
public class CustomSpringMvcExceptionHandler {

    private static List<String> getErrorMessages(MethodArgumentNotValidException exception) {
        List<String> messages = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .filter(Objects::nonNull)
                .toList();
        if (messages.isEmpty()) {
            messages = List.of("잘못된 요청입니다.");
        }
        return messages;
    }

    /**
     * MethodArgumentNotValidException: Bean Validation 오류
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<String> messages = getErrorMessages(exception);

        return ResponseEntity
                .status(VALIDATION_ERROR.status())
                .body(ErrorResponse.of(pathFrom(request), VALIDATION_ERROR.code(), messages));
    }

    /**
     * HttpMessageNotReadableException: 요청 body JSON 자체를 못 읽을 때. 예시: date: "2024/01/01"처럼 LocalDate 파싱 실패
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return validationError(request, "요청 본문 형식이 올바르지 않습니다.");
    }

    /**
     * MissingServletRequestParameterException: @RequestParam이 없을 때
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        return validationError(request, ex.getParameterName() + " 파라미터는 필수입니다.");
    }

    /**
     * MethodArgumentTypeMismatchException: @PathVariable, @RequestParam 타입 변환 실패
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            TypeMismatchException ex,
            HttpServletRequest request
    ) {
        return validationError(request, ex.getPropertyName() + " 값의 타입이 올바르지 않습니다.");
    }

    private ResponseEntity<ErrorResponse> validationError(HttpServletRequest request, String message) {
        return ResponseEntity
                .status(VALIDATION_ERROR.status())
                .body(ErrorResponse.of(pathFrom(request), VALIDATION_ERROR.code(), message));
    }

    private String pathFrom(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
