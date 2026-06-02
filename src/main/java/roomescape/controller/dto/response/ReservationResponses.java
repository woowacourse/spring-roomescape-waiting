package roomescape.controller.dto.response;

import roomescape.domain.Reservation;

import java.util.List;

public record ReservationResponses(
        List<ReservationResponse> reservations
) {
    public static ReservationResponses from(List<Reservation> reservations) {
        return new ReservationResponses(reservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }
}
