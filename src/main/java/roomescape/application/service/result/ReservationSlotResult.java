package roomescape.application.service.result;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;

public record ReservationSlotResult(
        long slotId,
        LocalDate date,
        ThemeRegisterResult theme,
        ReservationTimeResult time,
        ReservationResult reservation
) {
    public static ReservationSlotResult from(ReservationSlot slot, Reservation reservation) {
        return new ReservationSlotResult(
                slot.getId(),
                slot.getDate(),
                ThemeRegisterResult.from(slot.getTheme()),
                ReservationTimeResult.from(slot.getTime()),
                ReservationResult.from(reservation)
        );
    }
}
