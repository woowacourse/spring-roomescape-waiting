package roomescape.exception;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.exception.custom.status.CustomException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handlerIllegalArgument(
            final CustomException e
    ) {
        return ResponseEntity.status(e.getStatusValue())
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("잘못된 형식의 요청입니다."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNotValidRequest(
            final MethodArgumentNotValidException e,
            final BindingResult bindingResult
    ) {
        final String notValidField = generateNotValidFieldNames(bindingResult);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(notValidField + "의 값이 잘못된 형식입니다."));
    }

    private String generateNotValidFieldNames(final BindingResult bindingResult) {
        final String notValidField = bindingResult.getFieldErrors().stream()
                .map(FieldError::getField)
                .collect(Collectors.joining(", "));
        return notValidField;
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleNotCaughtExceptions(
            final Exception e
    ) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버에서 예기치 못한 예외가 발생하였습니다."));
    }
}
