package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationwaiting.service.ReservationWaitingService;

@Service
public class WaitingPromotionService {
    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public WaitingPromotionService(ReservationService reservationService,
                                   ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    @Transactional
    public void promoteFirstWaiting(Reservation cancelledReservation) {
        reservationWaitingService.findFirstWaiting(
                cancelledReservation.getDate(),
                cancelledReservation.getTheme().getId(),
                cancelledReservation.getTime().getId()
        ).ifPresent(waiting -> {
            reservationService.saveWith(
                    waiting.getName(),
                    waiting.getDate(),
                    cancelledReservation.getTheme(),
                    cancelledReservation.getTime()
            );

            reservationWaitingService.deleteById(waiting.getId());
        });
    }
}
