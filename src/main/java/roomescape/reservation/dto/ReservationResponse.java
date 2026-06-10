package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status
) {

    public static ReservationResponse from(Reservation reservation) {
        Slot slot = reservation.getSlot();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                ReservationTimeResponse.from(slot.getTime()),
                ThemeResponse.from(slot.getTheme()),
                reservation.getStatus().name()
        );
    }
}
