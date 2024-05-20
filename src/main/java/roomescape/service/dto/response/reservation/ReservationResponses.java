package roomescape.service.dto.response.reservation;

import java.util.List;
import roomescape.domain.Reservation;

public record ReservationResponses(List<ReservationResponse> reservations) {

    public static ReservationResponses from(List<Reservation> reservations) {
        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::new)
                .toList();
        return new ReservationResponses(responses);
    }
}
