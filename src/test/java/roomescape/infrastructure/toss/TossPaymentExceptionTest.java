package roomescape.infrastructure.toss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import roomescape.infrastructure.toss.dto.TossErrorResponse;

class TossPaymentExceptionTest {

    @Test
    @DisplayName("이미 처리된 결제 에러를 이미 승인된 예외로 매핑한다")
    void alreadyProcessed() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.BAD_REQUEST,
                new TossErrorResponse("ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제입니다.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.AlreadyProcessed.class);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getCode()).isEqualTo("ALREADY_PROCESSED_PAYMENT");
    }

    @Test
    @DisplayName("카드 거절 에러를 카드 거절 예외로 매핑한다")
    void cardRejected() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.FORBIDDEN,
                new TossErrorResponse("REJECT_CARD_PAYMENT", "카드 결제가 거절되었습니다.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.CardRejected.class);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getCode()).isEqualTo("REJECT_CARD_PAYMENT");
    }

    @Test
    @DisplayName("키 설정 오류를 게이트웨이 설정 예외로 매핑한다")
    void gatewayConfig() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.UNAUTHORIZED,
                new TossErrorResponse("INVALID_API_KEY", "잘못된 시크릿 키입니다.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.GatewayConfig.class);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getCode()).isEqualTo("INVALID_API_KEY");
    }

    @Test
    @DisplayName("토스 내부 오류를 재시도 대상 예외로 매핑한다")
    void retryable() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new TossErrorResponse("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", "일시적인 오류입니다.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.Retryable.class);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getCode()).isEqualTo("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING");
    }

    @Test
    @DisplayName("토스 호출량 초과 에러를 RateLimited 예외로 매핑한다")
    void rateLimited() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.TOO_MANY_REQUESTS,
                new TossErrorResponse("TOO_MANY_REQUESTS", "잠시 후 다시 시도해주세요.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.RateLimited.class);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exception.getCode()).isEqualTo("TOO_MANY_REQUESTS");
    }

    @Test
    @DisplayName("정의하지 않은 에러 코드는 기본 예외로 매핑한다")
    void unknown() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.BAD_REQUEST,
                new TossErrorResponse("UNKNOWN_PAYMENT_ERROR", "알 수 없는 오류입니다.")
        );

        assertThat(exception.getClass()).isEqualTo(TossPaymentException.class);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getCode()).isEqualTo("UNKNOWN_PAYMENT_ERROR");
    }
}
