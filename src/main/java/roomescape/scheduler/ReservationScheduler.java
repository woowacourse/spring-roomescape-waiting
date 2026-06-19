package roomescape.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.service.ReservationService;

@Component
public class ReservationScheduler {
    
    private final ReservationService reservationService;

    public ReservationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void deleteOldOrder() {
        reservationService.deleteEvictedReservations();
    }
}
