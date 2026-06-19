package roomescape.service;

import org.junit.jupiter.api.Test;
import roomescape.client.PaymentAmountMismatchException;
import roomescape.client.PaymentConfirmation;
import roomescape.client.PaymentGateway;
import roomescape.client.PaymentResult;
import roomescape.client.PaymentStatus;
import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.PaymentOrderRepository;
import roomescape.service.fake.FakeReservationRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceTest {

    @Test
    void 저장된_금액과_요청_금액이_다르면_승인_API를_호출하지_않는다() {
        FakePaymentOrderRepository orderRepository = new FakePaymentOrderRepository();
        ReservationRepository reservationRepository = new FakeReservationRepository();
        SpyPaymentGateway paymentGateway = new SpyPaymentGateway();
        PaymentService paymentService = new PaymentService(orderRepository, reservationRepository, paymentGateway);
        orderRepository.save(new PaymentOrder("order-1", 1L, 50_000L, null));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 10_000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        assertThat(paymentGateway.called).isFalse();
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().paymentKey()).isNull();
    }

    @Test
    void 저장된_금액과_요청_금액이_같으면_승인하고_paymentKey를_저장한다() {
        FakePaymentOrderRepository orderRepository = new FakePaymentOrderRepository();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        SpyPaymentGateway paymentGateway = new SpyPaymentGateway();
        PaymentService paymentService = new PaymentService(orderRepository, reservationRepository, paymentGateway);
        Reservation reservation = reservationRepository.save(pendingReservation());
        orderRepository.save(new PaymentOrder("order-1", reservation.getId(), 50_000L, null));

        PaymentResult result = paymentService.confirm("payment-key", "order-1", 50_000L);

        assertThat(paymentGateway.called).isTrue();
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().paymentKey()).isEqualTo("payment-key");
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void 결제_실패시_대기_예약과_주문을_정리한다() {
        FakePaymentOrderRepository orderRepository = new FakePaymentOrderRepository();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        SpyPaymentGateway paymentGateway = new SpyPaymentGateway();
        PaymentService paymentService = new PaymentService(orderRepository, reservationRepository, paymentGateway);
        Reservation reservation = reservationRepository.save(pendingReservation());
        orderRepository.save(new PaymentOrder("order-1", reservation.getId(), 50_000L, null));

        paymentService.cancelPending("order-1");

        assertThat(orderRepository.findByOrderId("order-1")).isEmpty();
        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    private Reservation pendingReservation() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "무서운 테마", "thumb.png");
        return Reservation.create("브라운", new Schedule(LocalDate.now().plusDays(1), time, theme), LocalDate.now().atStartOfDay());
    }

    private static class SpyPaymentGateway implements PaymentGateway {

        private boolean called;

        @Override
        public PaymentResult confirm(PaymentConfirmation confirmation) {
            called = true;
            return new PaymentResult(
                    confirmation.paymentKey(),
                    confirmation.orderId(),
                    PaymentStatus.DONE,
                    confirmation.amount()
            );
        }
    }

    private static class FakePaymentOrderRepository implements PaymentOrderRepository {

        private final Map<String, PaymentOrder> orders = new HashMap<>();

        @Override
        public void save(PaymentOrder order) {
            orders.put(order.orderId(), order);
        }

        @Override
        public Optional<PaymentOrder> findByOrderId(String orderId) {
            return Optional.ofNullable(orders.get(orderId));
        }

        @Override
        public Optional<PaymentOrder> findByReservationId(long reservationId) {
            return orders.values().stream()
                    .filter(order -> order.reservationId().equals(reservationId))
                    .findFirst();
        }

        @Override
        public void complete(String orderId, String paymentKey) {
            PaymentOrder order = orders.get(orderId);
            orders.put(orderId, new PaymentOrder(order.orderId(), order.reservationId(), order.amount(), paymentKey));
        }

        @Override
        public void deleteByOrderId(String orderId) {
            orders.remove(orderId);
        }
    }
}
