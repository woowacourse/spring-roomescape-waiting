package roomescape.reservation.dto;

import java.util.List;

public record UserReservationsResponse(
        List<UserReservationResponse> reservations,
        List<UserReservationResponse> waitings
) {

    public static UserReservationsResponse of(
            List<UserReservationResponse> reservations,
            List<UserReservationResponse> waitings
    ) {
        return new UserReservationsResponse(reservations, waitings);
    }
}
