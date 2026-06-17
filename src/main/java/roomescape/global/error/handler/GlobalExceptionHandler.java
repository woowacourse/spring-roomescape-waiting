package roomescape.global.error.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.feature.payment.PaymentException;
import roomescape.feature.payment.PaymentFailureType;
import roomescape.global.error.dto.ErrorResponseDto;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.dto.ParameterErrorResponsesDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpRequestMethodNotSupportedException(Exception e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ErrorResponseDto("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."));
    }

    @ExceptionHandler({
        NoHandlerFoundException.class,
        NoResourceFoundException.class
    })
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponseDto("NOT_FOUND", "존재하지 않는 API입니다."));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponseDto> handleIllegalRequestForm(
        Exception e
    ) {
        return ResponseEntity.badRequest().body(new ErrorResponseDto("INVALID_REQUEST", "요청 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ParameterErrorResponsesDto> handleConstraintViolationException(
        ConstraintViolationException e
    ) {
        List<ParameterErrorResponseDto> parameterErrors = e.getConstraintViolations()
            .stream()
            .map(violation -> new ParameterErrorResponseDto(
                getParameterName(violation),
                violation.getMessage()
            ))
            .toList();

        return ResponseEntity.badRequest()
            .body(new ParameterErrorResponsesDto("요청 값이 올바르지 않습니다.", parameterErrors));
    }

    private String getParameterName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastSeparatorIndex = propertyPath.lastIndexOf('.');

        if (lastSeparatorIndex == -1) {
            return propertyPath;
        }
        return propertyPath.substring(lastSeparatorIndex + 1);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ParameterErrorResponsesDto> handleValidationException(
        MethodArgumentNotValidException exception
    ) {
        List<ParameterErrorResponseDto> parameterErrors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ParameterErrorResponseDto(
                error.getField(),
                error.getDefaultMessage()
            ))
            .toList();

        return ResponseEntity.badRequest()
            .body(new ParameterErrorResponsesDto("요청 값이 올바르지 않습니다.", parameterErrors));
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponseDto> handleReservationException(GeneralException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponseDto(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(GeneralParametersException.class)
    public ResponseEntity<ParameterErrorResponsesDto> handleReservationNotFoundException(GeneralParametersException e) {
        return ResponseEntity.status(e.getStatus())
            .body(new ParameterErrorResponsesDto(e.getMessage(), e.getParameterErrors()));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentException(PaymentException e) {
        HttpStatus status = resolvePaymentStatus(e.getFailureType());
        // message 는 클라이언트 개발자 디버깅용(토스 원본 code + message). 사용자 노출 UX 는 클라이언트가 code 로 분기한다.
        String debugMessage = e.getCode() + " - " + e.getMessage();

        return ResponseEntity.status(status)
            .body(new ErrorResponseDto(e.getFailureType().name(), debugMessage));
    }

    private HttpStatus resolvePaymentStatus(PaymentFailureType failureType) {
        return switch (failureType) {
            case CARD_DECLINED -> HttpStatus.BAD_REQUEST;
            case RETRYABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case ALREADY_DONE -> HttpStatus.OK;
            case CLIENT_FAULT, UNKNOWN -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDto("INTERNAL_ERROR", "예상치 못한 오류가 발생했습니다."));
    }
}
