package roomescape.controller.dto;

import java.util.List;
import roomescape.domain.reservation.Reservation;

public record ReservationResponses(
        List<ReservationResponse> reservationResponses
) {

    public static ReservationResponses from(List<Reservation> reservations) {
        return new ReservationResponses(
                reservations.stream()
                        .map(ReservationResponse::from)
                        .toList()
        );
    }
}
