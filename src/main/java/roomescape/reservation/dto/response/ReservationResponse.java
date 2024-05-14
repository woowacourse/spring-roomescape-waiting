package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        String memberName,
        LocalDate date,
        ReservationTimeResponse time,
        ReservedThemeResponse theme
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMemberName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ReservedThemeResponse.from(reservation.getTheme())
        );
    }
}
