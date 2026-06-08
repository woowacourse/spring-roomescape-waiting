package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.controller.dto.response.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT.getDetail());
        return invalidInput(detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String detail = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(ErrorCode.INVALID_INPUT.getDetail());
        return invalidInput(detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable() {
        return invalidInput("요청 본문 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        return invalidInput(e.getName() + " 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        return invalidInput(e.getParameterName() + "는 필수입니다.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND;

        if (isHtmlRequest(request)) {
            ModelAndView modelAndView = new ModelAndView("error/404");
            modelAndView.setStatus(errorCode.getStatus());
            return modelAndView;
        }
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode, errorCode.getDetail()));
    }

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomescapeException(RoomescapeException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode, e.getMessage()));
    }

    @ExceptionHandler(TransientDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleTransientDataAccessException() {
        ErrorCode errorCode = ErrorCode.TEMPORARY_UNAVAILABLE;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode, errorCode.getDetail()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException() {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode, errorCode.getDetail()));
    }

    private ResponseEntity<ErrorResponse> invalidInput(String detail) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode, detail));
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
    }
}
