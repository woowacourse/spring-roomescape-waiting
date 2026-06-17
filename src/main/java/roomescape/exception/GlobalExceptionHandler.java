package roomescape.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.controller.dto.ErrorResponse;
import roomescape.domain.exception.InvalidDomainException;
import roomescape.exception.base.RoomeScapeException;
import roomescape.exception.base.RoomeScapeServerException;
import roomescape.infra.payment.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 커스텀 예외 통합 처리 - 예외가 추가돼도 핸들러이제 더 추가 안해도 됨.

    // 4xx 클라이언트 예외 통합 처리
    @ExceptionHandler(RoomeScapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomeScapeException(RoomeScapeException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ErrorResponse> handleTossPaymentException(TossPaymentException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponse(e.getMessage()));
    }

    // 5xx 서버 예외 통합 처리 - fallback으로 가지 않고, 의식적으로 잡은 500에러
    @ExceptionHandler(RoomeScapeServerException.class)
    public ResponseEntity<ErrorResponse> handleServerException(RoomeScapeServerException e) {
        log.error("서버 내부 오류 발생", e);// 내부에 해당 에러가 던진 "메시지 문자열"이 들어가 있어서 로그에 찍힘 !
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponse("서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }


    // 도메인 불변식 위반 (null, 빈 값, 형식 오류 등)
    @ExceptionHandler(InvalidDomainException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDomain(InvalidDomainException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    //아래 부터는 스프링이 던지는 요청 형식 예외
    //  @RequestBody  DTO 검증 실패( @Valid로 검증한 객체의 필드 제약 위반)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailed(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message));
    }

    // 컨트롤러 메서드 파라미터 검증 실패 (@RequestParam, @PathVariable 등의 제약 위반)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message));
    }


    // 요청 본문 파싱 실패 (잘못된 JSON, 잘못된 날짜 형식 등).
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("요청 본문의 형식이 올바르지 않습니다."));
    }

    // 경로/쿼리 파라미터의 타입 변환 실패 (themeId=abc, date=2026/13 등).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("요청 파라미터 형식이 올바르지 않습니다."));
    }

    // 필수 쿼리 파라미터 누락 (date 없이 가능 시간 조회 요청 등).
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("필수 요청 파라미터가 누락되었습니다."));
    }

    // 지원하지 않는 HTTP 메서드 (URL은 맞지만 메서드 잘못).
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("지원하지 않는 요청 방식입니다."));
    }

    //존재하지 않는 URL
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("요청하신 리소스를 찾을 수 없습니다."));
    }

    // fallback 마지막 그물망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("처리 중 예상치 못한 예외 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }


}
