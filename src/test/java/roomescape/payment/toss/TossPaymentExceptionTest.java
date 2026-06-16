package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class TossPaymentExceptionTest {

    @Test
    @DisplayName("카드 거절 코드는 CardRejected(403)로 번역된다")
    void cardRejected() {
        TossPaymentException e = TossPaymentException.of(
                HttpStatus.FORBIDDEN, new TossErrorResponse("REJECT_CARD_PAYMENT", "거절"));

        assertThat(e).isInstanceOf(TossPaymentException.CardRejected.class);
        assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(e.getCode()).isEqualTo("REJECT_CARD_PAYMENT");
    }

    @Test
    @DisplayName("키 오류 코드는 GatewayConfig로 번역된다")
    void gatewayConfig() {
        TossPaymentException e = TossPaymentException.of(
                HttpStatus.UNAUTHORIZED, new TossErrorResponse("UNAUTHORIZED_KEY", "key"));

        assertThat(e).isInstanceOf(TossPaymentException.GatewayConfig.class);
    }

    @Test
    @DisplayName("결제 건 없음 코드는 PaymentNotFound(404)로 번역된다")
    void paymentNotFound() {
        TossPaymentException e = TossPaymentException.of(
                HttpStatus.NOT_FOUND, new TossErrorResponse("NOT_FOUND_PAYMENT", "없음"));

        assertThat(e).isInstanceOf(TossPaymentException.PaymentNotFound.class);
    }

    @Test
    @DisplayName("미정의 코드는 기본 TossPaymentException으로 떨어지고 전달된 status/code를 유지한다")
    void undefinedCode() {
        TossPaymentException e = TossPaymentException.of(
                HttpStatus.BAD_GATEWAY, new TossErrorResponse("SOME_UNKNOWN_CODE", "msg"));

        assertThat(e.getClass()).isEqualTo(TossPaymentException.class);
        assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(e.getCode()).isEqualTo("SOME_UNKNOWN_CODE");
    }
}
