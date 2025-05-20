package roomescape.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationResponse(
        long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ReservationThemeResponse theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getMember().getName(),
                reservation.getDate(), ReservationTimeResponse.from(reservation.getTime()),
                ReservationThemeResponse.from(reservation.getTheme()));
    }

    public static ReservationResponse fromV2(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getMember().getName(), reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ReservationThemeResponse.from(reservation.getTheme()));
    }
}
