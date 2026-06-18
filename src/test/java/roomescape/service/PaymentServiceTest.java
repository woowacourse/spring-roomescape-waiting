package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.domain.Order;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.dto.payment.PaymentConfirmation;
import roomescape.dto.payment.PaymentResult;
import roomescape.exception.RoomescapeException;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;
import roomescape.repository.OrderRepository;

class PaymentServiceTest extends ServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

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
        assertThat(orderRepository.findByOrderId(order.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.DONE);
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