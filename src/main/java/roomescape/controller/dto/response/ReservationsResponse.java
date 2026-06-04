package roomescape.controller.dto.response;

import roomescape.domain.Reservation;

import java.util.List;

public record ReservationsResponse(
        List<ReservationResponse> reservations
) {

    public static ReservationsResponse from(List<Reservation> reservations) {
        return new ReservationsResponse(reservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }
}
