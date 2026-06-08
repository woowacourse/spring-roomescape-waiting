package roomescape.controller.dto.response;

import roomescape.service.dto.ReservationStatus;
import roomescape.service.dto.Status;

import java.time.LocalDate;

public record ReservationStatusResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ReservationThemeResponse theme,
        Status status,
        Long turn
) {

    public static ReservationStatusResponse from(ReservationStatus reservationStatus) {
        return new ReservationStatusResponse(
                reservationStatus.id(),
                reservationStatus.name(),
                reservationStatus.date(),
                ReservationTimeResponse.from(reservationStatus.time()),
                ReservationThemeResponse.from(reservationStatus.theme()),
                reservationStatus.status(),
                reservationStatus.turn()
        );
    }
}
