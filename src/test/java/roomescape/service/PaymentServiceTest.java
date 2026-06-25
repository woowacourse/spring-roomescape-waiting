package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reserver;
import roomescape.domain.Theme;
import roomescape.service.payment.PaymentAmountMismatchException;
import roomescape.service.payment.PaymentConfirmation;
import roomescape.service.payment.PaymentFailureCategory;
import roomescape.service.payment.PaymentGateway;
import roomescape.service.payment.PaymentGatewayException;
import roomescape.service.payment.PaymentResult;
import roomescape.repository.PaymentRepository;

class PaymentServiceTest {

    private final ReservationService reservationService = org.mockito.Mockito.mock();
    private final PaymentRepository paymentRepository = org.mockito.Mockito.mock();
    private final PaymentGateway paymentGateway = org.mockito.Mockito.mock();
    private final PaymentService paymentService = new PaymentService(reservationService, paymentRepository, paymentGateway);

    @Test
    void 사용자_예약과_결제_대기_정보를_생성한다() {
        LocalDate date = LocalDate.of(2099, 1, 1);
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 12, 0);
        Reservation reservation = pendingReservation(1L, date);
        when(reservationService.createPendingByUser("브라운", date, 1L, 1L, now)).thenReturn(reservation);
        when(paymentRepository.insert(any(Payment.class)))
                .thenAnswer(invocation -> invocation.<Payment>getArgument(0).withId(1L));

        Payment payment = paymentService.createForReservation("브라운", date, 1L, 1L, now);

        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getReservationId()).isEqualTo(1L);
        assertThat(payment.getAmount()).isEqualTo(20_000L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        verify(reservationService).createPendingByUser("브라운", date, 1L, 1L, now);
        verify(paymentRepository).insert(any(Payment.class));
    }

    @Test
    void 실패한_결제_뒤에는_새_결제를_생성한다() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 12, 0);
        Reservation reservation = pendingReservation(1L, LocalDate.of(2099, 1, 1));
        Payment failedPayment = Payment.restore(1L, 1L, "payment_failed_12345678901234567890", 20_000L, null,
                PaymentStatus.FAILED, "REJECT_CARD_PAYMENT", "카드가 거절되었습니다.");
        when(reservationService.findPendingByUser(1L, "브라운", now)).thenReturn(reservation);
        when(paymentRepository.findLatestByReservationId(1L)).thenReturn(Optional.of(failedPayment));
        when(paymentRepository.insert(any(Payment.class)))
                .thenAnswer(invocation -> invocation.<Payment>getArgument(0).withId(2L));

        Payment payment = paymentService.resumeOrRetryForReservation(1L, "브라운", now);

        assertThat(payment.getId()).isEqualTo(2L);
        assertThat(payment.getReservationId()).isEqualTo(1L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        verify(reservationService).findPendingByUser(1L, "브라운", now);
        verify(paymentRepository).findLatestByReservationId(1L);
        verify(paymentRepository).insert(any(Payment.class));
    }

    @Test
    void 준비된_결제가_있으면_기존_결제를_반환한다() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 12, 0);
        Reservation reservation = pendingReservation(1L, LocalDate.of(2099, 1, 1));
        Payment readyPayment = Payment.restore(1L, 1L, "payment_ready_123456789012345678901", 20_000L, null,
                PaymentStatus.READY, null, null);
        when(reservationService.findPendingByUser(1L, "브라운", now)).thenReturn(reservation);
        when(paymentRepository.findLatestByReservationId(1L)).thenReturn(Optional.of(readyPayment));

        Payment result = paymentService.resumeOrRetryForReservation(1L, "브라운", now);

        assertThat(result).isSameAs(readyPayment);
        verify(paymentRepository, never()).insert(any());
    }

    @Test
    void 주문번호로_결제에_연결된_예약을_조회한다() {
        Payment payment = readyPayment();
        Reservation reservation = pendingReservation(1L, LocalDate.of(2099, 1, 1));
        when(paymentRepository.findByOrderId(payment.getOrderId())).thenReturn(Optional.of(payment));
        when(reservationService.findById(1L)).thenReturn(reservation);

        Reservation result = paymentService.findReservationByOrderId(payment.getOrderId());

        assertThat(result).isSameAs(reservation);
    }

    @Test
    void 결제번호로_결제에_연결된_예약을_조회한다() {
        Payment payment = readyPayment();
        Reservation reservation = pendingReservation(1L, LocalDate.of(2099, 1, 1));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(reservationService.findById(1L)).thenReturn(reservation);

        Reservation result = paymentService.findReservationByPaymentId(1L);

        assertThat(result).isSameAs(reservation);
    }

    @Test
    void 저장된_금액과_다르면_결제_승인을_요청하지_않는다() {
        Payment payment = readyPayment();
        when(paymentRepository.findByOrderId(payment.getOrderId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirm("test_payment_key", payment.getOrderId(), 19_000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
        verify(paymentRepository, never()).update(any());
    }

    @Test
    void 결제_승인이_완료되면_결제와_예약을_확정한다() {
        Payment payment = readyPayment();
        PaymentConfirmation confirmation = new PaymentConfirmation("test_payment_key", payment.getOrderId(), 20_000L);
        PaymentResult result = new PaymentResult("test_payment_key", payment.getOrderId(), PaymentStatus.CONFIRMED, 20_000L);
        when(paymentRepository.findByOrderId(payment.getOrderId())).thenReturn(Optional.of(payment));
        when(paymentGateway.confirm(confirmation)).thenReturn(result);

        PaymentResult actual = paymentService.confirm("test_payment_key", payment.getOrderId(), 20_000L);

        assertThat(actual).isEqualTo(result);
        verify(paymentGateway).confirm(confirmation);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).update(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getPaymentKey()).isEqualTo("test_payment_key");
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        verify(reservationService).confirmPayment(1L);
    }

    @Test
    void 확정적인_승인_실패는_결제를_실패_상태로_저장한다() {
        Payment payment = readyPayment();
        PaymentConfirmation confirmation = new PaymentConfirmation("test_payment_key", payment.getOrderId(), 20_000L);
        PaymentGatewayException exception = new PaymentGatewayException(
                PaymentFailureCategory.DEFINITIVE, "REJECT_CARD_PAYMENT", "카드가 거절되었습니다.");
        when(paymentRepository.findByOrderId(payment.getOrderId())).thenReturn(Optional.of(payment));
        when(paymentGateway.confirm(confirmation)).thenThrow(exception);

        assertThatThrownBy(() -> paymentService.confirm("test_payment_key", payment.getOrderId(), 20_000L))
                .isSameAs(exception);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).update(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(paymentCaptor.getValue().getFailureCode()).isEqualTo("REJECT_CARD_PAYMENT");
        verify(reservationService, never()).confirmPayment(any());
    }

    @Test
    void 결과가_불명확한_승인_실패는_결제_상태를_바꾸지_않는다() {
        Payment payment = readyPayment();
        PaymentConfirmation confirmation = new PaymentConfirmation("test_payment_key", payment.getOrderId(), 20_000L);
        PaymentGatewayException exception = new PaymentGatewayException(
                PaymentFailureCategory.UNKNOWN, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", "토스 내부 오류입니다.");
        when(paymentRepository.findByOrderId(payment.getOrderId())).thenReturn(Optional.of(payment));
        when(paymentGateway.confirm(confirmation)).thenThrow(exception);

        assertThatThrownBy(() -> paymentService.confirm("test_payment_key", payment.getOrderId(), 20_000L))
                .isSameAs(exception);

        verify(paymentRepository, never()).update(any());
        verify(reservationService, never()).confirmPayment(any());
    }

    @Test
    void 결제_실패를_저장하고_결제_대기_예약은_유지한다() {
        Payment payment = readyPayment();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        paymentService.fail(1L, "REJECT_CARD_PAYMENT", "카드가 거절되었습니다.");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).update(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(paymentCaptor.getValue().getFailureCode()).isEqualTo("REJECT_CARD_PAYMENT");
        verify(reservationService, never()).confirmPayment(any());
    }

    @Test
    void 사용자_취소는_취소된_결제_상태로_저장한다() {
        Payment payment = readyPayment();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        paymentService.fail(1L, "PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).update(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    private Reservation pendingReservation(Long id, LocalDate date) {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new Reservation(id, new Reserver("브라운"), new ReservationSlot(date, time, theme),
                ReservationStatus.PENDING);
    }

    private Payment readyPayment() {
        return Payment.restore(1L, 1L, "payment_ready_123456789012345678901", 20_000L, null,
                PaymentStatus.READY, null, null);
    }
}
