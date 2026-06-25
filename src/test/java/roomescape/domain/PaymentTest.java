package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void 결제_대기_정보를_생성한다() {
        Payment payment = Payment.ready(1L, 20_000L);

        assertThat(payment.getReservationId()).isEqualTo(1L);
        assertThat(payment.getAmount()).isEqualTo(20_000L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getOrderId()).matches("[A-Za-z0-9_-]{6,64}");
    }

    @Test
    void 예약_id가_양수가_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> Payment.ready(0L, 20_000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reservationId는 양수여야 합니다.");
    }

    @Test
    void 승인된_결제는_paymentKey가_필요하다() {
        assertThatThrownBy(() -> Payment.restore(1L, 1L, "payment_confirmed_123456789012345", 20_000L,
                null, PaymentStatus.CONFIRMED, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("승인된 결제는 paymentKey가 필요합니다.");
    }

    @Test
    void 실패한_결제는_실패_코드가_필요하다() {
        assertThatThrownBy(() -> Payment.restore(1L, 1L, "payment_failed_123456789012345678", 20_000L,
                null, PaymentStatus.FAILED, null, "카드 거절"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("실패 또는 취소된 결제는 실패 코드가 필요합니다.");
    }
}
