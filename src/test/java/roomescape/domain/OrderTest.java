package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.OrderStatus;

class OrderTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 주문 객체가 생성된다.")
    void 주문_생성() {
        Order order = new Order(1L, "order-1", 50000L, 1L, OrderStatus.PENDING);

        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getOrderId()).isEqualTo("order-1");
        assertThat(order.getAmount()).isEqualTo(50000L);
        assertThat(order.getReservationId()).isEqualTo(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문 번호가 null이거나 공백이면 예외가 발생한다.")
    void 주문_번호_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Order(null, 50000L, 1L))
                .withMessage("주문 번호는 필수입니다.");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Order(" ", 50000L, 1L))
                .withMessage("주문 번호는 필수입니다.");
    }

    @Test
    @DisplayName("주문 금액이 null이거나 음수이면 예외가 발생한다.")
    void 주문_금액_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Order("order-1", null, 1L))
                .withMessage("주문 금액은 0 이상이어야 합니다.");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Order("order-1", -1L, 1L))
                .withMessage("주문 금액은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("예약 식별자가 null이면 예외가 발생한다.")
    void 예약_식별자_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Order("order-1", 50000L, null))
                .withMessage("예약 식별자는 필수입니다.");
    }

    @Test
    @DisplayName("주문 상태가 null이면 예외가 발생한다.")
    void 주문_상태_예외_발생() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Order(1L, "order-1", 50000L, 1L, null))
                .withMessage("주문 상태는 필수입니다.");
    }
}
