package roomescape.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.ReservationFixture.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.Order;
import roomescape.domain.OrderStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ConflictException;

class OrderTest {

    private static final Reservation RESERVATION = reservation(
            "민욱",
            LocalDate.of(2026, 8, 6),
            new ReservationTime(1L, LocalTime.of(11, 0)),
            new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg")
    );

    @Test
    void 주문은_결제_대기_상태로_생성된다() {
        Order order = order();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getPaymentKey()).isNull();
    }

    @Test
    void 결제를_확정하면_paymentKey를_저장하고_CONFIRMED로_변경한다() {
        Order order = order();

        order.confirm("payment-key");

        assertThat(order.getPaymentKey()).isEqualTo("payment-key");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void 빈_paymentKey로는_결제를_확정할_수_없다() {
        Order order = order();

        assertThatThrownBy(() -> order.confirm(" "))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void 이미_확정된_주문을_다시_확정할_수_없다() {
        Order order = order();
        order.confirm("payment-key");

        assertThatThrownBy(() -> order.confirm("another-payment-key"))
                .isInstanceOf(ConflictException.class);
    }

    private Order order() {
        return new Order(
                "order-id",
                "공포 예약",
                50_000L,
                RESERVATION
        );
    }
}
