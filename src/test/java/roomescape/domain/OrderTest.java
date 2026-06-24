package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class OrderTest {

    private final Reservation reservation = new Reservation(
            1L,
            new Member(1L, "브라운"),
            new ReservationSlot(
                    LocalDate.of(2026, 6, 1),
                    new ReservationTime(1L, LocalTime.of(10, 0)),
                    new Theme(1L, "방탈출1", "설명", "https://thumb.com")
            )
    );

    @Test
    void orderId가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Order(null, null, 10000L, reservation))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void amount가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Order(null, "order-1", null, reservation))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void reservation이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Order(null, "order-1", 10000L, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 생성_시_idempotencyKey가_자동으로_부여된다() {
        Order order = Order.createWithoutId("order-1", 10000L, reservation);

        assertThat(order.getIdempotencyKey()).isNotNull().isNotBlank();
    }

    @Test
    void 같은_주문을_여러_번_생성해도_idempotencyKey는_서로_다르다() {
        Order order1 = Order.createWithoutId("order-1", 10000L, reservation);
        Order order2 = Order.createWithoutId("order-2", 10000L, reservation);

        assertThat(order1.getIdempotencyKey()).isNotEqualTo(order2.getIdempotencyKey());
    }

    @Test
    void paymentKey는_초기에_null이다() {
        Order order = Order.createWithoutId("order-1", 10000L, reservation);

        assertThat(order.getPaymentKey()).isNull();
    }

    @Test
    void updatePaymentKey로_paymentKey를_저장할_수_있다() {
        Order order = Order.createWithoutId("order-1", 10000L, reservation);

        order.updatePaymentKey("pk_test_abc123");

        assertThat(order.getPaymentKey()).isEqualTo("pk_test_abc123");
    }

    @Test
    void id가_모두_존재하면_id로_동등성을_비교한다() {
        Order order1 = new Order(1L, "order-1", 10000L, reservation);
        Order order2 = new Order(1L, "order-2", 20000L, reservation);

        assertThat(order1).isEqualTo(order2);
    }

    @Test
    void id가_다르면_동등하지_않다() {
        Order order1 = new Order(1L, "order-1", 10000L, reservation);
        Order order2 = new Order(2L, "order-1", 10000L, reservation);

        assertThat(order1).isNotEqualTo(order2);
    }

    @Test
    void id가_없으면_orderId와_amount와_reservation으로_동등성을_비교한다() {
        Order order1 = new Order(null, "order-1", 10000L, reservation);
        Order order2 = new Order(null, "order-1", 10000L, reservation);

        assertThat(order1).isEqualTo(order2);
    }

    @Test
    void id가_없고_orderId가_다르면_동등하지_않다() {
        Order order1 = new Order(null, "order-1", 10000L, reservation);
        Order order2 = new Order(null, "order-2", 10000L, reservation);

        assertThat(order1).isNotEqualTo(order2);
    }
}
