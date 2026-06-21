package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.domain.Order;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.exception.RoomescapeException;
import roomescape.infrastructure.payment.PaymentUnknownException;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;
import roomescape.repository.OrderRepository;
import roomescape.repository.ReservationRepository;

class PaymentServiceTest extends ServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Test
    @DisplayName("결제를 승인하면 게이트웨이 응답 상태로 주문 상태를 갱신한다")
    void confirmUpdatesOrderStatus() {
        Order order = createOrder();
        String orderId = order.getOrderId().getValue();
        given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .willReturn(new PaymentResult("payment-key", orderId, PaymentStatus.DONE, 50000L));

        PaymentResult result = paymentService.confirm("payment-key", orderId, 50000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        Order updated = orderRepository.findByOrderId(order.getOrderId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(updated.getPaymentKey()).isEqualTo("payment-key");
    }

    @Test
    @DisplayName("요청 금액이 주문 금액과 다르면 예외를 던진다")
    void confirmThrowsWhenAmountMismatch() {
        Order order = createOrder();
        String orderId = order.getOrderId().getValue();

        assertThatThrownBy(() -> paymentService.confirm("payment-key", orderId, 9999L))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("존재하지 않는 주문 번호로 승인하면 예외를 던진다")
    void confirmThrowsWhenOrderNotFound() {
        assertThatThrownBy(() -> paymentService.confirm("payment-key", "non-existent-order", 50000L))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("승인 결과가 불명확하면(read timeout) 주문을 UNCONFIRMED로 남기고 예외를 다시 던진다")
    void confirmMarksUnconfirmedWhenResultUnknown() {
        Order order = createOrder();
        String orderId = order.getOrderId().getValue();
        willThrow(new PaymentUnknownException("결제 결과를 확인하지 못했습니다.", null))
                .given(paymentGateway).confirm(any(PaymentConfirmation.class));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", orderId, 50000L))
                .isInstanceOf(PaymentUnknownException.class);

        Order updated = orderRepository.findByOrderId(order.getOrderId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.UNCONFIRMED);
        assertThat(updated.getPaymentKey()).isEqualTo("payment-key");
    }

    @Test
    @DisplayName("결제 대기 주문을 정리하면 주문과 예약이 함께 삭제된다")
    void cancelPendingOrderDeletesOrderAndReservation() {
        Order order = createOrder();

        paymentService.cancelPendingOrder(order.getOrderId().getValue());

        assertThat(orderRepository.findByOrderId(order.getOrderId())).isEmpty();
        assertThat(reservationRepository.findById(order.getReservationId())).isEmpty();
    }

    @Test
    @DisplayName("이미 승인된(READY 아님) 주문은 정리 대상에서 제외한다")
    void cancelPendingOrderKeepsConfirmedOrder() {
        Order order = createOrder();
        String orderId = order.getOrderId().getValue();
        given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .willReturn(new PaymentResult("payment-key", orderId, PaymentStatus.DONE, 50000L));
        paymentService.confirm("payment-key", orderId, 50000L);

        paymentService.cancelPendingOrder(orderId);

        assertThat(orderRepository.findByOrderId(order.getOrderId())).isPresent();
        assertThat(reservationRepository.findById(order.getReservationId())).isPresent();
    }

    @Test
    @DisplayName("orderId가 없으면(사용자 취소) 아무것도 하지 않는다")
    void cancelPendingOrderIgnoresNullOrderId() {
        paymentService.cancelPendingOrder(null);
        paymentService.cancelPendingOrder("  ");
    }

    private Order createOrder() {
        insertDefaultStore();
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "테마");
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        long reservationId = DbFixtures.insertReservation(
                jdbcTemplate, userId, themeId, "2026-05-08", timeId, DEFAULT_STORE_ID);
        Reservation reservation = Fixtures.sampleReservation(reservationId);
        return orderService.create(reservation);
    }
}