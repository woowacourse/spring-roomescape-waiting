package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;

public record ReservationResponse(
        long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ReservationThemeResponse theme,
        String status) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getReservationItem().getDate(),
                ReservationTimeResponse.from(reservation.getReservationItem().getTime()),
                ReservationThemeResponse.from(reservation.getReservationItem().getTheme()),
                reservation.getReservationStatus().description);
    }
}
