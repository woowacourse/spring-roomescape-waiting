package roomescape.service.dto.response.reservation;

import java.util.List;
import roomescape.domain.Reservation;

public record UserReservationResponses(List<UserReservationResponse> reservations) {

    public static UserReservationResponses from(List<Reservation> reservations) {
        List<UserReservationResponse> responses = reservations.stream()
                .map(UserReservationResponse::new)
                .toList();
        return new UserReservationResponses(responses);
    }
}
