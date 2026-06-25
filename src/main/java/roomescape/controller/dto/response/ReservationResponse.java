package roomescape.controller.dto.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ReservationThemeResponse theme,
        ReservationStatus status
) {

    public static ReservationResponse from(Reservation reservation) {
        ReservationSlot slot = reservation.getSlot();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                ReservationTimeResponse.from(slot.getTime()),
                ReservationThemeResponse.from(slot.getTheme()),
                reservation.getStatus()
        );
    }
}
