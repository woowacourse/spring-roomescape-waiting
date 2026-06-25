package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentOrderTest {

    @Test
    @DisplayName("주문 번호를 토스 멱등키로 사용하며 주문에 고정되고 300자를 넘지 않는다")
    void idempotencyKey() {
        PaymentOrder paymentOrder = PaymentOrder.create(1L, 10_000L);

        assertThat(paymentOrder.getIdempotencyKey()).isEqualTo(paymentOrder.getOrderId());
        assertThat(paymentOrder.getIdempotencyKey()).hasSizeLessThanOrEqualTo(PaymentOrder.MAX_IDEMPOTENCY_KEY_LENGTH);
        assertThat(paymentOrder.getIdempotencyKey()).matches("[0-9a-f]{32}");
        assertThat(paymentOrder.getIdempotencyKey()).isEqualTo(paymentOrder.getIdempotencyKey());
    }
}
