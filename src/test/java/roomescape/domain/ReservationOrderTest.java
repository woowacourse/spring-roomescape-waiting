package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.PaymentAmountMismatchException;

class ReservationOrderTest {

    @Test
    void create로_생성된_주문은_미확정_상태이다() {
        ReservationOrder order = ReservationOrder.create(10000, 1L);

        assertThat(order.isConfirmed()).isFalse();
        assertThat(order.getPaymentKey()).isNull();
    }

    @Test
    void create는_요구사항_형식의_orderId를_생성한다() {
        ReservationOrder order = ReservationOrder.create(10000, 1L);

        assertThat(order.getId()).matches("[A-Za-z0-9_-]{6,64}");
    }

    @Test
    void verifyAmount는_금액이_일치하면_통과한다() {
        ReservationOrder order = ReservationOrder.restore("order-1", 10000, null, 1L);

        assertThatCode(() -> order.verifyAmount(10000)).doesNotThrowAnyException();
    }

    @Test
    void verifyAmount는_금액이_다르면_예외가_발생한다() {
        ReservationOrder order = ReservationOrder.restore("order-1", 10000, null, 1L);

        assertThatThrownBy(() -> order.verifyAmount(20000))
                .isInstanceOf(PaymentAmountMismatchException.class);
    }

    @Test
    void confirm은_paymentKey를_부여하고_확정_상태로_전이한다() {
        ReservationOrder order = ReservationOrder.restore("order-1", 10000, null, 1L);

        ReservationOrder confirmed = order.confirm("pk_test");

        assertThat(confirmed.isConfirmed()).isTrue();
        assertThat(confirmed.getPaymentKey()).isEqualTo("pk_test");
        assertThat(confirmed.getId()).isEqualTo("order-1");
        assertThat(confirmed.getAmount()).isEqualTo(10000);
        assertThat(confirmed.getReservationId()).isEqualTo(1L);
    }

    @Test
    void 이미_확정된_주문을_confirm하면_예외가_발생한다() {
        ReservationOrder order = ReservationOrder.restore("order-1", 10000, "pk_old", 1L);

        assertThatThrownBy(() -> order.confirm("pk_new"))
                .isInstanceOf(AlreadyProcessedException.class);
    }
}
