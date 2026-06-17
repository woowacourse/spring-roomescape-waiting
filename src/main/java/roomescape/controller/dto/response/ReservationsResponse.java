package roomescape.controller.dto.response;

import roomescape.domain.Reservation;
import roomescape.service.dto.UserReservation;

import java.util.List;

public record ReservationsResponse(
        List<ReservationResponse> reservations
) {

    public static ReservationsResponse from(List<Reservation> reservations) {
        return new ReservationsResponse(reservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }

    public static ReservationsResponse fromUserReservations(List<UserReservation> reservations) {
        return new ReservationsResponse(reservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }
}
