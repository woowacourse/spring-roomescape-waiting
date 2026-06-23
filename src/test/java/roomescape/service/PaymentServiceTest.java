package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reserver;
import roomescape.domain.Theme;
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

    private Reservation pendingReservation(Long id, LocalDate date) {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new Reservation(id, new Reserver("브라운"), new ReservationSlot(date, time, theme),
                ReservationStatus.PENDING);
    }
}
