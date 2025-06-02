package roomescape.application.reservation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.WaitingRepository;

@Component
public class WaitingPromotion {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public WaitingPromotion(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public void promoteTopWaiting(ReservationSlot reservationSlot) {
        waitingRepository.findTopByReservationSlotOrderByStartedAtAsc(reservationSlot)
                .ifPresent(waiting -> {
                    Reservation reservation = waiting.toReservation();
                    reservationRepository.save(reservation);
                    waitingRepository.delete(waiting);
                });
    }
}
