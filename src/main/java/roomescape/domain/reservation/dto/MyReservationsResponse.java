package roomescape.domain.reservation.dto;

import java.util.List;
import roomescape.domain.reservation.Reservation;

public record MyReservationsResponse(
        List<MyReservationResponse> reservations
) {

    public static MyReservationsResponse from(List<Reservation> reservations) {
        return new MyReservationsResponse(reservations.stream()
                .map(MyReservationResponse::from)
                .toList()
        );
    }
}
