package roomescape.global.error.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.feature.payment.PaymentConnectionException;
import roomescape.feature.payment.PaymentException;
import roomescape.feature.payment.PaymentFailureType;
import roomescape.feature.payment.PaymentRateLimitedException;
import roomescape.feature.payment.PaymentTimeoutException;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.global.ratelimit.RateLimitException;
import roomescape.global.error.dto.ErrorResponseDto;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.dto.ParameterErrorResponsesDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    class ConstraintViolationException_처리 {

        @Test
        @SuppressWarnings("unchecked")
        void BAD_REQUEST를_반환하고_parameterErrors로_각_위반을_내려준다() {
            ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("deleteTime.id");
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn("id의 값은 양수여야 합니다.");

            ResponseEntity<ParameterErrorResponsesDto> response =
                handler.handleConstraintViolationException(new ConstraintViolationException(Set.of(violation)));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message()).isEqualTo("요청 값이 올바르지 않습니다.");
            assertThat(response.getBody().parameterErrors())
                .extracting(ParameterErrorResponseDto::parameter)
                .containsExactly("id");
        }
    }

    @Nested
    class MethodArgumentNotValidException_처리 {

        @Test
        void BAD_REQUEST를_반환하고_parameterErrors로_각_필드_오류를_내려준다() throws Exception {
            Object target = new Object();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
            bindingResult.addError(new FieldError("request", "value", "예약자명은 필수입니다."));
            bindingResult.addError(new FieldError("request", "date", "예약 날짜가 현재보다 과거입니다."));
            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                createMethodParameter(), bindingResult);

            ResponseEntity<ParameterErrorResponsesDto> response = handler.handleValidationException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message()).isEqualTo("요청 값이 올바르지 않습니다.");
            assertThat(response.getBody().parameterErrors())
                .containsExactly(
                    new ParameterErrorResponseDto("value", "예약자명은 필수입니다."),
                    new ParameterErrorResponseDto("date", "예약 날짜가 현재보다 과거입니다.")
                );
        }
    }

    @Nested
    class 부적절한_요청_형식_처리 {

        @Test
        void HttpMessageNotReadableException은_BAD_REQUEST와_message만_내려준다() {
            HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "본문을 읽을 수 없습니다.", new MockHttpInputMessage(new byte[0]));

            ResponseEntity<ErrorResponseDto> response =
                handler.handleIllegalRequestForm(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(new ErrorResponseDto("INVALID_REQUEST", "요청 형식이 올바르지 않습니다."));
        }

        @Test
        void HandlerMethodValidationException은_BAD_REQUEST와_message만_내려준다() {
            HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);

            ResponseEntity<ErrorResponseDto> response =
                handler.handleIllegalRequestForm(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(new ErrorResponseDto("INVALID_REQUEST", "요청 형식이 올바르지 않습니다."));
        }

        @Test
        void MethodArgumentTypeMismatchException은_BAD_REQUEST와_message만_내려준다() throws Exception {
            MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", createMethodParameter(), new IllegalArgumentException());

            ResponseEntity<ErrorResponseDto> response =
                handler.handleIllegalRequestForm(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(new ErrorResponseDto("INVALID_REQUEST", "요청 형식이 올바르지 않습니다."));
        }

        @Test
        void MissingServletRequestParameterException은_BAD_REQUEST와_message만_내려준다() {
            MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("value", "String");

            ResponseEntity<ErrorResponseDto> response =
                handler.handleIllegalRequestForm(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(new ErrorResponseDto("INVALID_REQUEST", "요청 형식이 올바르지 않습니다."));
        }
    }

    @Nested
    class GeneralException_처리 {

        @Test
        void BAD_REQUEST_에러_타입은_400과_message를_반환한다() {
            GeneralException exception = new GeneralException(ReservationErrorType.ILLEGAL_RESERVER_NAME);

            ResponseEntity<ErrorResponseDto> response = handler.handleReservationException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message()).isEqualTo("예약자명이 부적절합니다.");
        }

        @Test
        void FORBIDDEN_에러_타입은_403과_message를_반환한다() {
            GeneralException exception = new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN);

            ResponseEntity<ErrorResponseDto> response = handler.handleReservationException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().message()).isEqualTo("예약을 변경할 권한이 없습니다.");
        }

        @Test
        void NOT_FOUND_에러_타입은_404와_message를_반환한다() {
            GeneralException exception = new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);

            ResponseEntity<ErrorResponseDto> response = handler.handleReservationException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().message()).isEqualTo("예약을 찾을 수 없습니다.");
        }

        @Test
        void CONFLICT_에러_타입은_409와_message를_반환한다() {
            GeneralException exception = new GeneralException(ReservationErrorType.ALREADY_RESERVED);

            ResponseEntity<ErrorResponseDto> response = handler.handleReservationException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody().message()).isEqualTo("이미 예약된 날짜, 시간, 테마입니다.");
        }
    }

    @Nested
    class GeneralParametersException_처리 {

        @Test
        void 에러_타입의_상태코드와_parameterErrors를_반환한다() {
            GeneralParametersException exception = new GeneralParametersException(
                ReservationErrorType.FIELD_RESOURCE_NOT_FOUND,
                List.of(
                    new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."),
                    new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다.")
                )
            );

            ResponseEntity<ParameterErrorResponsesDto> response = handler.handleReservationNotFoundException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().message()).isEqualTo("조회할 자원이 존재하지 않습니다.");
            assertThat(response.getBody().parameterErrors())
                .containsExactly(
                    new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."),
                    new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다.")
                );
        }
    }

    @Nested
    class PaymentException_처리 {

        @Test
        void CARD_DECLINED는_400과_failureType을_code로_반환한다() {
            PaymentException exception = new PaymentException(
                PaymentFailureType.CARD_DECLINED, "REJECT_CARD_COMPANY", "결제 승인이 거절되었습니다.");

            ResponseEntity<ErrorResponseDto> response = handler.handlePaymentException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().code()).isEqualTo("CARD_DECLINED");
        }

        @Test
        void CLIENT_FAULT는_500과_failureType을_code로_반환한다() {
            PaymentException exception = new PaymentException(
                PaymentFailureType.CLIENT_FAULT, "INVALID_API_KEY", "잘못된 시크릿키 연동 정보 입니다.");

            ResponseEntity<ErrorResponseDto> response = handler.handlePaymentException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().code()).isEqualTo("CLIENT_FAULT");
        }

        @Test
        void RETRYABLE는_503을_반환한다() {
            PaymentException exception = new PaymentException(
                PaymentFailureType.RETRYABLE, "PROVIDER_ERROR", "일시적인 오류입니다.");

            ResponseEntity<ErrorResponseDto> response = handler.handlePaymentException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Nested
    class 전송_계층_실패_처리 {

        @Test
        void 연결_실패는_503과_PAYMENT_GATEWAY_UNREACHABLE을_반환한다() {
            PaymentConnectionException exception = new PaymentConnectionException(new RuntimeException("connect refused"));

            ResponseEntity<ErrorResponseDto> response = handler.handlePaymentConnectionException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getBody().code()).isEqualTo("PAYMENT_GATEWAY_UNREACHABLE");
        }

        @Test
        void 읽기_타임아웃은_504와_PAYMENT_RESULT_UNKNOWN을_반환한다() {
            PaymentTimeoutException exception = new PaymentTimeoutException(new RuntimeException("read timed out"));

            ResponseEntity<ErrorResponseDto> response = handler.handlePaymentTimeoutException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
            assertThat(response.getBody().code()).isEqualTo("PAYMENT_RESULT_UNKNOWN");
        }
    }

    @Nested
    class RateLimit_처리 {

        @Test
        void 아웃바운드_한도_초과는_503과_Retry_After_헤더를_반환한다() {
            RateLimitException exception = new RateLimitException("아웃바운드 요청 속도 제한 초과", 5L);

            ResponseEntity<ErrorResponseDto> response = handler.handleRateLimitException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("5");
            assertThat(response.getBody().code()).isEqualTo("RATE_LIMITED");
        }

        @Test
        void 토스_429_소진은_503과_Retry_After_헤더를_반환한다() {
            PaymentRateLimitedException exception = new PaymentRateLimitedException(3, 2L);

            ResponseEntity<ErrorResponseDto> response = handler.handlePaymentRateLimitedException(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("2");
            assertThat(response.getBody().code()).isEqualTo("PAYMENT_RATE_LIMITED");
        }
    }

    private MethodParameter createMethodParameter() throws NoSuchMethodException {
        return new MethodParameter(
            GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleMethod", String.class),
            0
        );
    }

    @SuppressWarnings("unused")
    private void sampleMethod(String value) {
    }
}
