package roomescape.reservation.service;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PendingReservationExpirer {

    private static final Duration EXPIRE_AFTER = Duration.ofDays(1);

    private final ReservationService reservationService;

    public PendingReservationExpirer(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedRate = 3_600_000)
    public void expire() {
        reservationService.expirePendingCreatedBefore(LocalDateTime.now().minus(EXPIRE_AFTER));
    }
}