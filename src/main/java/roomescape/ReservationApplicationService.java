package roomescape;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.reservationwaiting.service.ReservationWaitingService;

@Service
public class ReservationApplicationService {
    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationApplicationService(
            final ReservationService reservationService,
            final ReservationWaitingService reservationWaitingService
    ) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    public ReservationWaiting saveWaiting(String name, LocalDate date, Long themeId, Long timeId) {
        Reservation reservation = reservationService.findByDateAndThemeIdAndTimeId(date, themeId, timeId);
        return reservationWaitingService.save(name, reservation);
    }
}
