package roomescape.application.reservation.event;

import java.time.LocalDate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.command.AutoWaitingPromotionService;

@Component
public class DeleteReservationEventListener {

    private final AutoWaitingPromotionService autoWaitingPromotionService;

    public DeleteReservationEventListener(AutoWaitingPromotionService autoWaitingPromotionService) {
        this.autoWaitingPromotionService = autoWaitingPromotionService;
    }

    @EventListener
    @Transactional
    public void handle(ReservationCancelEvent reservationCancelEvent) {
        LocalDate date = reservationCancelEvent.reservationDate();
        Long reservationTimeId = reservationCancelEvent.reservationTimeId();
        Long themeId = reservationCancelEvent.themeId();
        autoWaitingPromotionService.promote(date, reservationTimeId, themeId);
    }
}
