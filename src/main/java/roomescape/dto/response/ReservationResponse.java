package roomescape.dto.response;

import roomescape.domain.Reservation;

import java.time.LocalDate;

public record ReservationResponse(
        long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                ReservationStatus.RESERVED
        );
    }
}
