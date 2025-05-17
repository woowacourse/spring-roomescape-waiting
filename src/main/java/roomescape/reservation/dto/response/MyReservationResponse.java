package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;

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
                reservation.themeName(),
                reservation.getDate().toString(),
                reservation.reservationTime().toString(),
                "예약"
        );
    }
}
