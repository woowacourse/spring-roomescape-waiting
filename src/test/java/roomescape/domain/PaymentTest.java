package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.payment.Payment;

class PaymentTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 승인된 결제 객체가 생성된다.")
    void 결제_생성() {
        Payment payment = new Payment(1L, "payment-key", "order-1");

        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getPaymentKey()).isEqualTo("payment-key");
        assertThat(payment.getOrderId()).isEqualTo("order-1");
    }

    @Test
    @DisplayName("결제 키가 null이거나 공백이면 예외가 발생한다.")
    void 결제_키_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment(null, "order-1"))
                .withMessage("결제 키는 필수입니다.");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment(" ", "order-1"))
                .withMessage("결제 키는 필수입니다.");
    }

    @Test
    @DisplayName("주문 번호가 null이거나 공백이면 예외가 발생한다.")
    void 주문_번호_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment("payment-key", null))
                .withMessage("주문 번호는 필수입니다.");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Payment("payment-key", " "))
                .withMessage("주문 번호는 필수입니다.");
    }
}
