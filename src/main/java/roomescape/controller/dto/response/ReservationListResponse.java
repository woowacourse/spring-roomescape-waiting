package roomescape.controller.dto.response;

import java.util.List;
import roomescape.domain.Reservation;

public record ReservationListResponse(
        List<ReservationResponse> items
) {
    public static ReservationListResponse from(List<Reservation> reservations) {
        return new ReservationListResponse(reservations.stream()
                .map(ReservationResponse::from)
                .toList()
        );
    }
}
