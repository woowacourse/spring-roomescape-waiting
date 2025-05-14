package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        String date,
        String time,
        String status
) {
    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate().toString(),
                reservation.getReservationTime().toString(),
                "예약"
        );
    }
}
