package roomescape.reservation.presentation.dto.response;

import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String status) {

    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate().toString(),
                reservation.getTime().getStartAt().toString(), reservation.getReservationStatus().getName());
    }
}
