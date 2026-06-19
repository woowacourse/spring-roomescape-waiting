package roomescape.service;

import org.junit.jupiter.api.Test;
import roomescape.client.PaymentAmountMismatchException;
import roomescape.client.PaymentAlreadyProcessedException;
import roomescape.client.PaymentConfirmation;
import roomescape.client.PaymentConfirmationUnknownException;
import roomescape.client.PaymentFailureException;
import roomescape.client.PaymentGateway;
import roomescape.client.PaymentGatewayRetryableException;
import roomescape.client.PaymentResult;
import roomescape.client.PaymentStatus;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;
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
        orderRepository.save(new PaymentOrder("order-1", 1L, 50_000L, "idempotency-key-1", null, PaymentOrderStatus.PENDING));

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
        orderRepository.save(new PaymentOrder("order-1", reservation.getId(), 50_000L, "idempotency-key-1", null, PaymentOrderStatus.PENDING));

        PaymentResult result = paymentService.confirm("payment-key", "order-1", 50_000L);

        assertThat(paymentGateway.called).isTrue();
        assertThat(paymentGateway.confirmation.idempotencyKey()).isEqualTo("idempotency-key-1");
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().paymentKey()).isEqualTo("payment-key");
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().status()).isEqualTo(PaymentOrderStatus.CONFIRMED);
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
        orderRepository.save(new PaymentOrder("order-1", reservation.getId(), 50_000L, "idempotency-key-1", null, PaymentOrderStatus.PENDING));

        paymentService.cancelPending("order-1");

        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().status()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
    }

    @Test
    void 승인_응답을_받지_못하면_paymentKey를_저장하고_확인_필요_상태로_남긴다() {
        FakePaymentOrderRepository orderRepository = new FakePaymentOrderRepository();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        UnknownPaymentGateway paymentGateway = new UnknownPaymentGateway();
        PaymentService paymentService = new PaymentService(orderRepository, reservationRepository, paymentGateway);
        Reservation reservation = reservationRepository.save(pendingReservation());
        orderRepository.save(new PaymentOrder("order-1", reservation.getId(), 50_000L, "idempotency-key-1", null, PaymentOrderStatus.PENDING));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 50_000L))
                .isInstanceOf(PaymentConfirmationUnknownException.class);

        PaymentOrder order = orderRepository.findByOrderId("order-1").orElseThrow();
        assertThat(order.paymentKey()).isEqualTo("payment-key");
        assertThat(order.status()).isEqualTo(PaymentOrderStatus.UNKNOWN);
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void 승인_API가_명시적_실패를_응답하면_주문을_실패_상태로_남긴다() {
        FakePaymentOrderRepository orderRepository = new FakePaymentOrderRepository();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        PaymentService paymentService = new PaymentService(
                orderRepository,
                reservationRepository,
                confirmation -> {
                    throw new PaymentFailureException("REJECT_CARD_PAYMENT", "카드가 거절되었습니다.");
                }
        );
        Reservation reservation = reservationRepository.save(pendingReservation());
        orderRepository.save(new PaymentOrder("order-1", reservation.getId(), 50_000L, "idempotency-key-1", null, PaymentOrderStatus.PENDING));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 50_000L))
                .isInstanceOf(PaymentFailureException.class);

        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().status()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().paymentKey()).isEqualTo("payment-key");
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void 이미_처리됨이나_재시도성_오류는_확인_필요_상태로_남긴다() {
        FakePaymentOrderRepository orderRepository = new FakePaymentOrderRepository();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        PaymentService paymentService = new PaymentService(
                orderRepository,
                reservationRepository,
                confirmation -> {
                    throw new PaymentGatewayRetryableException("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", "일시 오류");
                }
        );
        Reservation reservation = reservationRepository.save(pendingReservation());
        orderRepository.save(new PaymentOrder("order-1", reservation.getId(), 50_000L, "idempotency-key-1", null, PaymentOrderStatus.PENDING));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 50_000L))
                .isInstanceOf(PaymentGatewayRetryableException.class);

        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().status()).isEqualTo(PaymentOrderStatus.UNKNOWN);
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().paymentKey()).isEqualTo("payment-key");
    }

    @Test
    void 이미_처리된_결제_응답은_확인_필요_상태로_남긴다() {
        FakePaymentOrderRepository orderRepository = new FakePaymentOrderRepository();
        FakeReservationRepository reservationRepository = new FakeReservationRepository();
        PaymentService paymentService = new PaymentService(
                orderRepository,
                reservationRepository,
                confirmation -> {
                    throw new PaymentAlreadyProcessedException("이미 처리된 결제입니다.");
                }
        );
        Reservation reservation = reservationRepository.save(pendingReservation());
        orderRepository.save(new PaymentOrder("order-1", reservation.getId(), 50_000L, "idempotency-key-1", null, PaymentOrderStatus.PENDING));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 50_000L))
                .isInstanceOf(PaymentAlreadyProcessedException.class);

        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().status()).isEqualTo(PaymentOrderStatus.UNKNOWN);
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().paymentKey()).isEqualTo("payment-key");
    }

    private Reservation pendingReservation() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "무서운 테마", "thumb.png");
        return Reservation.create("브라운", new Schedule(LocalDate.now().plusDays(1), time, theme), LocalDate.now().atStartOfDay());
    }

    private static class SpyPaymentGateway implements PaymentGateway {

        private boolean called;
        private PaymentConfirmation confirmation;

        @Override
        public PaymentResult confirm(PaymentConfirmation confirmation) {
            called = true;
            this.confirmation = confirmation;
            return new PaymentResult(
                    confirmation.paymentKey(),
                    confirmation.orderId(),
                    PaymentStatus.DONE,
                    confirmation.amount()
            );
        }
    }

    private static class UnknownPaymentGateway implements PaymentGateway {

        @Override
        public PaymentResult confirm(PaymentConfirmation confirmation) {
            throw new PaymentConfirmationUnknownException("확인 필요", new RuntimeException());
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
        public void recordPaymentKey(String orderId, String paymentKey) {
            PaymentOrder order = orders.get(orderId);
            orders.put(orderId, new PaymentOrder(
                    order.orderId(),
                    order.reservationId(),
                    order.amount(),
                    order.idempotencyKey(),
                    paymentKey,
                    order.status()
            ));
        }

        @Override
        public void complete(String orderId, String paymentKey) {
            PaymentOrder order = orders.get(orderId);
            orders.put(orderId, new PaymentOrder(
                    order.orderId(),
                    order.reservationId(),
                    order.amount(),
                    order.idempotencyKey(),
                    paymentKey,
                    PaymentOrderStatus.CONFIRMED
            ));
        }

        @Override
        public void markUnknown(String orderId) {
            PaymentOrder order = orders.get(orderId);
            orders.put(orderId, new PaymentOrder(
                    order.orderId(),
                    order.reservationId(),
                    order.amount(),
                    order.idempotencyKey(),
                    order.paymentKey(),
                    PaymentOrderStatus.UNKNOWN
            ));
        }

        @Override
        public void markFailed(String orderId) {
            PaymentOrder order = orders.get(orderId);
            orders.put(orderId, new PaymentOrder(
                    order.orderId(),
                    order.reservationId(),
                    order.amount(),
                    order.idempotencyKey(),
                    order.paymentKey(),
                    PaymentOrderStatus.FAILED
            ));
        }
    }
}
