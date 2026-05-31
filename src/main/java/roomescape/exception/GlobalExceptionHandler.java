package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return invalidInput(detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException e) {
        String detail = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return invalidInput(detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable() {
        return invalidInput("요청 본문 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        return invalidInput(e.getName() + " 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e) {
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
        return error(errorCode);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException e) {
        return error(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException() {
        return error(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ProblemDetail> invalidInput(String detail) {
        return error(ErrorCode.INVALID_INPUT, detail);
    }

    private ResponseEntity<ProblemDetail> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getMessage());
    }

    private ResponseEntity<ProblemDetail> error(ErrorCode errorCode, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);
        problemDetail.setTitle(errorCode.getMessage());
        problemDetail.setProperty("code", errorCode.name());
        return ResponseEntity.status(errorCode.getStatus())
                .body(problemDetail);
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
    }
}
