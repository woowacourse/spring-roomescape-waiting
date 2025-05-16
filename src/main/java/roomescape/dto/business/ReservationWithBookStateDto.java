package roomescape.dto.business;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ThemeResponse;

public record ReservationWithBookStateDto(
        Long id,
        LocalDate date,
        String statusText,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public ReservationWithBookStateDto(Reservation reservation) {
        this(reservation.getId(),
                reservation.getDate(),
                reservation.getStatus().getDisplayName(),
                new ReservationTimeResponse(reservation.getReservationTime()),
                new ThemeResponse(reservation.getTheme())
        );
    }
}
