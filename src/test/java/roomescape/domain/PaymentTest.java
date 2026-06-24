package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.payment.Payment;

class PaymentTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 결제 객체가 생성된다.")
    void 결제_생성() {
        Payment payment = new Payment(1L, "order-1", "payment-key", 50000L, 1L);

        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getOrderId()).isEqualTo("order-1");
        assertThat(payment.getPaymentKey()).isEqualTo("payment-key");
        assertThat(payment.getAmount()).isEqualTo(50000L);
        assertThat(payment.getReservationId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("결제 승인 전에는 paymentKey 없이 결제 객체를 생성할 수 있다.")
    void 승인_전_결제_생성() {
        Payment payment = new Payment("order-1", 50000L, 1L);

        assertThat(payment.getId()).isNull();
        assertThat(payment.getPaymentKey()).isNull();
    }

    @Test
    @DisplayName("주문 번호가 null이거나 공백이면 예외가 발생한다.")
    void 주문_번호_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment(null, 50000L, 1L))
                .withMessage("주문 번호는 필수입니다.");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment(" ", 50000L, 1L))
                .withMessage("주문 번호는 필수입니다.");
    }

    @Test
    @DisplayName("결제 금액이 null이거나 음수이면 예외가 발생한다.")
    void 결제_금액_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment("order-1", null, 1L))
                .withMessage("결제 금액은 0 이상이어야 합니다.");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment("order-1", -1L, 1L))
                .withMessage("결제 금액은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("예약 식별자가 null이면 예외가 발생한다.")
    void 예약_식별자_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment("order-1", 50000L, null))
                .withMessage("예약 식별자는 필수입니다.");
    }
}
