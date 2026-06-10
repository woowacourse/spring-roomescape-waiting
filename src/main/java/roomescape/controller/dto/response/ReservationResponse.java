package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status
) implements ReservationWaitResponse {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                ReservationStatus.CONFIRMED
        );
    }
}
