package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.order.Order;
import roomescape.order.OrderStatus;

class OrderTest {

    @Test
    @DisplayName("저장된 금액과 다른 금액으로 검증하면 예외를 던진다")
    void validateAmountMismatch() {
        Order order = Order.create("order-1", "idem-1", 1L, 30000L);

        assertThatThrownBy(() -> order.validateAmount(20000L))
                .isInstanceOf(PaymentAmountMismatchException.class);
    }

    @Test
    @DisplayName("저장된 금액과 같은 금액으로 검증하면 통과한다")
    void validateAmountMatch() {
        Order order = Order.create("order-1", "idem-1", 1L, 30000L);

        assertThatCode(() -> order.validateAmount(30000L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("승인 완료 시 paymentKey가 저장되고 상태가 CONFIRMED가 된다")
    void complete() {
        Order order = Order.create("order-1", "idem-1", 1L, 30000L);

        order.complete("payment-key-1");

        assertThat(order.getPaymentKey()).isEqualTo("payment-key-1");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("이미 완료된 주문을 다시 완료하면 예외를 던진다")
    void completeTwice() {
        Order order = Order.create("order-1", "idem-1", 1L, 30000L);
        order.complete("payment-key-1");

        assertThatThrownBy(() -> order.complete("payment-key-2"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
