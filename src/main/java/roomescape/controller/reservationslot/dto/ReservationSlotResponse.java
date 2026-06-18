package roomescape.controller.reservationslot.dto;

import java.time.LocalDate;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.domain.reservationslot.ReservationSlot;

public record ReservationSlotResponse(
        Long id,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time
) {
    public static ReservationSlotResponse from(final ReservationSlot slot) {
        return new ReservationSlotResponse(
                slot.getId(),
                slot.getDate(),
                ThemeResponse.from(slot.getTheme()),
                ReservationTimeResponse.from(slot.getTime())
        );
    }
}
