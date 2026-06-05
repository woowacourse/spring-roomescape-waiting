package roomescape.domain.service;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWaiting;

import java.util.List;
import java.util.Optional;

@Component
public class WaitingPromotionService {
    public Optional<WaitingPromotionResult> promote(List<ReservationWaiting> waitings) {
        if (waitings.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new WaitingPromotionResult(waitings.get(0), reservationFrom(waitings.get(0))));
    }

    private Reservation reservationFrom(ReservationWaiting waiting) {
        return Reservation.promote(
                waiting.getUserName(),
                waiting.getSlot()
        );
    }
}
