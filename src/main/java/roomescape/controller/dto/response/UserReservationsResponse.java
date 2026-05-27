package roomescape.controller.dto.response;

import roomescape.service.dto.UserReservation;

import java.util.List;

public record UserReservationsResponse(
        List<UserReservationResponse> userReservations
) {

    public static UserReservationsResponse from(List<UserReservation> userReservations) {
        return new UserReservationsResponse(userReservations.stream()
                .map(UserReservationResponse::from)
                .toList());
    }
}
