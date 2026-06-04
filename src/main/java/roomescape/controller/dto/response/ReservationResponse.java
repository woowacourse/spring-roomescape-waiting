package roomescape.controller.dto.response;

import roomescape.domain.Reservation;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getSchedule().getDate(),
                ReservationTimeResponse.from(reservation.getSchedule().getTime()),
                ThemeResponse.from(reservation.getSchedule().getTheme())
        );
    }
}
