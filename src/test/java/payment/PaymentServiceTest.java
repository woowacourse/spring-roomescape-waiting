package payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import payment.order.Order;
import payment.order.OrderRepository;
import roomescape.RoomescapeApplication;
import roomescape.controller.FixedClockConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.repository.ReservationDao;
import roomescape.service.PendingReservation;
import roomescape.service.ReservationCommandService;

@SpringBootTest(classes = RoomescapeApplication.class)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PaymentServiceTest {

    @Autowired
    private ReservationCommandService reservationCommandService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ReservationDao reservationDao;
    @MockitoBean
    private PaymentGateway paymentGateway;

    @Test
    @DisplayName("콜백 금액이 저장된 주문 금액과 다르면 결제 승인 요청을 보내지 않는다.")
    void amountMismatchDoesNotCallGateway() {
        PendingReservation pending = reservationCommandService.createPendingPaymentReservation(
                "new-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        assertThatThrownBy(() ->
                paymentService.confirm("payment-key", pending.orderId(), 49_000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
        assertThat(reservationDao.findById(pending.reservation().id()).orElseThrow().status())
                .isEqualTo(ReservationStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("결제 승인 성공 시 주문에 paymentKey를 저장하고 예약을 확정한다.")
    void confirmSuccess() {
        PendingReservation pending = reservationCommandService.createPendingPaymentReservation(
                "new-user", LocalDate.of(2026, 6, 5), 1L, 2L);
        when(paymentGateway.confirm(new PaymentConfirmation("payment-key", pending.orderId(), 5_000L)))
                .thenReturn(new PaymentResult("payment-key", pending.orderId(), 5_000L, PaymentStatus.DONE));

        PaymentResult result = paymentService.confirm("payment-key", pending.orderId(), 5_000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        Order order = orderRepository.findByOrderId(pending.orderId()).orElseThrow();
        assertThat(order.paymentKey()).isEqualTo("payment-key");
        assertThat(order.status()).isEqualTo(PaymentStatus.DONE);
        Reservation reservation = reservationDao.findById(pending.reservation().id()).orElseThrow();
        assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("실패 콜백에서 orderId가 없으면 저장소를 조회하지 않고 종료한다.")
    void failWithoutOrderId() {
        paymentService.fail("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", null);
    }

    @Test
    @DisplayName("실패 콜백에서 orderId가 있으면 결제대기 예약을 삭제한다.")
    void failWithOrderIdDeletesPendingReservation() {
        PendingReservation pending = reservationCommandService.createPendingPaymentReservation(
                "new-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        paymentService.fail("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", pending.orderId());

        assertThat(reservationDao.findById(pending.reservation().id())).isEmpty();
    }
}
