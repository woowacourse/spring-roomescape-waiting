package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;

public record MyReservationOutput(Long reservationId,
                                  String theme,
                                  String date,
                                  String time) {

    public static MyReservationOutput from(final Reservation reservation) {
        return new MyReservationOutput(reservation.getId(), reservation.getInfo().getTheme().getName(),
                reservation.getInfo().getDate().toString(),
                reservation.getInfo().getTime().getStartAt().toString());
    }
}
