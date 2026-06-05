package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationWithWaitingOrder(
        Long id,
        String name,
        ReservationSlot slot,
        Long waitingOrder
) {
    public LocalDate date() {
        return slot.getDate();
    }

    public ReservationTime time() {
        return slot.getTime();
    }

    public Theme theme() {
        return slot.getTheme();
    }
}
