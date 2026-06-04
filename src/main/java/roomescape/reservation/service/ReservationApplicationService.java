package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;

@Service
@RequiredArgsConstructor
public class ReservationApplicationService {

    private final ReservationService reservationService;
    private final WaitingPromotionService waitingPromotionService;
    private final Clock clock;

    public void cancelReservationByIdAndPromoteWaiting(final long reservationId) {
        final Reservation reservation = reservationService.getReservation(reservationId);
        reservationService.cancel(reservation.getId());

        promoteIfFutureSlot(reservation);
    }

    public void deleteReservationById(final long reservationId) {
        final Reservation reservation = reservationService.getReservation(reservationId);
        reservationService.deleteById(reservationId);

        promoteIfFutureSlot(reservation);
    }

    private void promoteIfFutureSlot(final Reservation reservation) {
        if (!reservation.isFutureSlot(LocalDateTime.now(clock))) {
            return;
        }

        waitingPromotionService.promoteBySlot(
            reservation.getDate(),
            reservation.getTimeId(),
            reservation.getThemeId()
        );
    }
}
