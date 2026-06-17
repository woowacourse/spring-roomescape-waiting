package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.order.Order;
import roomescape.order.OrderService;
import roomescape.order.OrderStatus;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentService;
import roomescape.reservation.ReservationService;

/**
 * PaymentService는 이제 조율만 한다 — 주문 상태는 OrderService, 예약 상태는 ReservationService가 소유.
 * 따라서 테스트도 "올바르게 위임하는가"를 검증한다(상태 변이 자체는 각 서비스 테스트의 몫).
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private OrderService orderService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private ReservationAuthorizationService authorizationService;
    @InjectMocks
    private PaymentService paymentService;

    private final Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);

    @Test
    @DisplayName("금액이 일치하면 승인 후 주문 확정·예약 확정을 각 서비스에 위임한다")
    void confirmSuccess() {
        Order order = Order.reconstruct(1L, "order-1", 10L, 30000L, null, OrderStatus.PENDING);
        given(orderService.findByOrderId("order-1")).willReturn(Optional.of(order));
        given(paymentGateway.confirm(any())).willReturn(new PaymentResult("pk-1", "order-1", "DONE", 30000L));

        paymentService.confirm(member, "pk-1", "order-1", 30000L);

        verify(paymentGateway).confirm(any());
        verify(orderService).complete(order, "pk-1");
        verify(reservationService).confirm(10L);
    }

    @Test
    @DisplayName("조작된 금액은 검증에서 막히고 게이트웨이·확정 위임이 일어나지 않는다")
    void confirmAmountMismatchBlocksGateway() {
        Order order = Order.reconstruct(1L, "order-1", 10L, 30000L, null, OrderStatus.PENDING);
        given(orderService.findByOrderId("order-1")).willReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.confirm(member, "pk-1", "order-1", 20000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
        verify(reservationService, never()).confirm(anyLong());
    }

    @Test
    @DisplayName("만료 정리(expire)는 주문 실패 처리와 예약 취소를 각 서비스에 위임한다")
    void expireAbandonedOrder() {
        Order order = Order.reconstruct(1L, "order-1", 10L, 30000L, null, OrderStatus.PENDING);
        given(orderService.findByOrderId("order-1")).willReturn(Optional.of(order));

        paymentService.expire("order-1");

        verify(orderService).markFailed(order);
        verify(reservationService).cancelPending(10L);
    }

    @Test
    @DisplayName("이미 확정된 주문은 만료 정리에서 건너뛴다(위임 없음)")
    void expireSkipsConfirmed() {
        Order order = Order.reconstruct(1L, "order-1", 10L, 30000L, "pk", OrderStatus.CONFIRMED);
        given(orderService.findByOrderId("order-1")).willReturn(Optional.of(order));

        paymentService.expire("order-1");

        verify(orderService, never()).markFailed(any());
        verify(reservationService, never()).cancelPending(anyLong());
    }
}
