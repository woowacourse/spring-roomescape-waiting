package roomescape.service.dto.result;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.slot.EventSlot;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult timeResult,
        ThemeResult themeResult
) {

    public static ReservationResult from(Reservation reservation) {
        EventSlot slot = reservation.getEventSlot();
        return new ReservationResult(
                reservation.getId(),
                reservation.getName().value(),
                slot.date(),
                ReservationTimeResult.from(slot.time()),
                ThemeResult.from(slot.theme())
        );
    }
}
