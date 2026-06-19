package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.exception.PaymentException.PaymentNotFoundException;
import roomescape.fake.FakeReservationOrderRepository;

class ReservationOrderServiceTest {

    private FakeReservationOrderRepository orderRepository;
    private ReservationOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = new FakeReservationOrderRepository();
        service = new ReservationOrderService(orderRepository);
    }

    @Test
    void 주문이_생성되면_금액과_예약id가_저장된다() {
        ReservationOrder order = service.insert(1L);

        assertThat(order.getAmount()).isEqualTo(10000);
        assertThat(order.getReservationId()).isEqualTo(1L);
        assertThat(orderRepository.findById(order.getId())).isPresent();
    }

    @Test
    void getByOrderId는_존재하는_주문을_반환한다() {
        orderRepository.save(ReservationOrder.restore("order-1", 10000, null, 1L));

        assertThat(service.getByOrderId("order-1").getId()).isEqualTo("order-1");
    }

    @Test
    void getByOrderId는_존재하지_않으면_예외가_발생한다() {
        assertThatThrownBy(() -> service.getByOrderId("no-order"))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void completeOrder는_paymentKey를_저장한다() {
        orderRepository.save(ReservationOrder.restore("order-1", 10000, null, 1L));
        ReservationOrder order = service.getByOrderId("order-1");

        service.completeOrder(order, "pk_test");

        assertThat(orderRepository.findById("order-1")).get()
                .extracting(ReservationOrder::getPaymentKey).isEqualTo("pk_test");
    }
}
