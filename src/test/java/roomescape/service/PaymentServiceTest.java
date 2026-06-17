package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.dao.OrderDao;
import roomescape.reservation.ReservationDao;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.OrderStatus;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentService;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;
import roomescape.store.Store;
import roomescape.theme.Theme;
import roomescape.time.Time;
import roomescape.common.vo.Name;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderDao orderDao;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private ReservationDao reservationDao;
    @Mock
    private ReservationAuthorizationService authorizationService;
    @InjectMocks
    private PaymentService paymentService;

    private final Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);

    private Reservation pendingReservation(Long id) {
        Time time = new Time(1L, LocalTime.of(13, 0));
        Theme theme = new Theme(1L, new Name("테마"), "http://url", "설명", 30000L);
        Store store = new Store(1L, "강남점");
        return Reservation.reconstruct(id, member, LocalDate.now().plusDays(1), time, theme,
                ReservationStatus.PENDING, null, 0L, store);
    }

    @Test
    @DisplayName("금액이 일치하면 승인 후 주문이 CONFIRMED, 예약이 BOOKED가 된다")
    void confirmSuccess() {
        Order order = Order.reconstruct(1L, "order-1", 10L, 30000L, null, OrderStatus.PENDING);
        Reservation reservation = pendingReservation(10L);
        given(orderDao.findByOrderId("order-1")).willReturn(Optional.of(order));
        given(paymentGateway.confirm(any())).willReturn(new PaymentResult("pk-1", "order-1", "DONE", 30000L));
        given(reservationDao.findById(10L)).willReturn(Optional.of(reservation));

        paymentService.confirm(member, "pk-1", "order-1", 30000L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getPaymentKey()).isEqualTo("pk-1");
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.BOOKED);
        verify(paymentGateway).confirm(any());
        verify(orderDao).update(order);
        verify(reservationDao).update(reservation);
    }

    @Test
    @DisplayName("조작된 금액은 검증에서 막히고 게이트웨이가 호출되지 않는다")
    void confirmAmountMismatchBlocksGateway() {
        Order order = Order.reconstruct(1L, "order-1", 10L, 30000L, null, OrderStatus.PENDING);
        given(orderDao.findByOrderId("order-1")).willReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.confirm(member, "pk-1", "order-1", 20000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    @DisplayName("만료 정리(expire)는 PENDING 주문을 FAILED로, 예약을 CANCELED로 만든다 (abandon 재사용)")
    void expireAbandonedOrder() {
        Order order = Order.reconstruct(1L, "order-1", 10L, 30000L, null, OrderStatus.PENDING);
        Reservation reservation = pendingReservation(10L);
        given(orderDao.findByOrderId("order-1")).willReturn(Optional.of(order));
        given(reservationDao.findById(10L)).willReturn(Optional.of(reservation));

        paymentService.expire("order-1");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        verify(orderDao).update(order);
        verify(reservationDao).update(reservation);
    }

    @Test
    @DisplayName("이미 확정된 주문은 만료 정리에서 건너뛴다")
    void expireSkipsConfirmed() {
        Order order = Order.reconstruct(1L, "order-1", 10L, 30000L, "pk", OrderStatus.CONFIRMED);
        given(orderDao.findByOrderId("order-1")).willReturn(Optional.of(order));

        paymentService.expire("order-1");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderDao, never()).update(any());
        verify(reservationDao, never()).update(any());
    }
}
