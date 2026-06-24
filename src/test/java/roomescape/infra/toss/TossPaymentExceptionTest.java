package roomescape.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.infra.toss.dto.TossErrorResponse;

class TossPaymentExceptionTest {

    @Test
    @DisplayName("이미 처리된 결제 에러를 전용 예외로 변환한다.")
    void 이미_처리된_결제_예외() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.BAD_REQUEST,
                new TossErrorResponse("ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제입니다.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.AlreadyProcessed.class);
        assertThat(exception.getCode()).isEqualTo("ALREADY_PROCESSED_PAYMENT");
    }

    @Test
    @DisplayName("카드 거절 에러를 전용 예외로 변환한다.")
    void 카드_거절_예외() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.FORBIDDEN,
                new TossErrorResponse("REJECT_CARD_PAYMENT", "카드 결제가 거절되었습니다.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.CardRejected.class);
        assertThat(exception.getCode()).isEqualTo("REJECT_CARD_PAYMENT");
    }

    @Test
    @DisplayName("키 오류를 게이트웨이 설정 예외로 변환한다.")
    void 키_오류_예외() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.UNAUTHORIZED,
                new TossErrorResponse("UNAUTHORIZED_KEY", "인증되지 않은 시크릿 키입니다.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.GatewayConfig.class);
        assertThat(exception.getCode()).isEqualTo("UNAUTHORIZED_KEY");
    }

    @Test
    @DisplayName("재시도 대상 에러를 전용 예외로 변환한다.")
    void 재시도_대상_예외() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new TossErrorResponse("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", "일시적인 오류입니다.")
        );

        assertThat(exception).isInstanceOf(TossPaymentException.Retryable.class);
        assertThat(exception.getCode()).isEqualTo("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING");
    }

    @Test
    @DisplayName("정의하지 않은 에러 코드는 기본 Toss 결제 예외로 변환한다.")
    void 미정의_코드_기본_예외() {
        TossPaymentException exception = TossPaymentException.of(
                HttpStatus.BAD_REQUEST,
                new TossErrorResponse("UNKNOWN_PAYMENT_ERROR", "알 수 없는 결제 오류입니다.")
        );

        assertThat(exception.getClass()).isEqualTo(TossPaymentException.class);
        assertThat(exception.getCode()).isEqualTo("UNKNOWN_PAYMENT_ERROR");
    }
}
