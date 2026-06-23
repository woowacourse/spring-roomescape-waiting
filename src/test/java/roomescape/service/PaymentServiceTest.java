package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reserver;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.PaymentRepository;

class PaymentServiceTest {

    private final ReservationService reservationService = org.mockito.Mockito.mock();
    private final PaymentRepository paymentRepository = org.mockito.Mockito.mock();
    private final PaymentService paymentService = new PaymentService(reservationService, paymentRepository);

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
        Payment failedPayment = new Payment(1L, 1L, "payment_failed_12345678901234567890", 20_000L, null,
                PaymentStatus.FAILED, "REJECT_CARD_PAYMENT", "카드가 거절되었습니다.");
        when(reservationService.findPendingByUser(1L, "브라운", now)).thenReturn(reservation);
        when(paymentRepository.findLatestByReservationId(1L)).thenReturn(Optional.of(failedPayment));
        when(paymentRepository.insert(any(Payment.class)))
                .thenAnswer(invocation -> invocation.<Payment>getArgument(0).withId(2L));

        Payment payment = paymentService.retryForReservation(1L, "브라운", now);

        assertThat(payment.getId()).isEqualTo(2L);
        assertThat(payment.getReservationId()).isEqualTo(1L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        verify(reservationService).findPendingByUser(1L, "브라운", now);
        verify(paymentRepository).findLatestByReservationId(1L);
        verify(paymentRepository).insert(any(Payment.class));
    }

    @Test
    void 진행_중인_결제가_있으면_재결제를_막는다() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 12, 0);
        Reservation reservation = pendingReservation(1L, LocalDate.of(2099, 1, 1));
        Payment readyPayment = new Payment(1L, 1L, "payment_ready_123456789012345678901", 20_000L, null,
                PaymentStatus.READY, null, null);
        when(reservationService.findPendingByUser(1L, "브라운", now)).thenReturn(reservation);
        when(paymentRepository.findLatestByReservationId(1L)).thenReturn(Optional.of(readyPayment));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> paymentService.retryForReservation(1L, "브라운", now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_RETRY_NOT_ALLOWED);
    }

    private Reservation pendingReservation(Long id, LocalDate date) {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new Reservation(id, new Reserver("브라운"), new ReservationSlot(date, time, theme),
                ReservationStatus.PENDING);
    }
}
