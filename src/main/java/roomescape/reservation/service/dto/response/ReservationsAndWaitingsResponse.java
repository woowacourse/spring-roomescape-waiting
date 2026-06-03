package roomescape.reservation.service.dto.response;

import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.service.dto.response.WaitingResponse;

public record ReservationsAndWaitingsResponse(
    List<ReservationResponse> reservations,
    List<WaitingResponse> waitings
) {

    public static ReservationsAndWaitingsResponse from(
        final List<Reservation> reservations,
        final List<WaitingResponse> waitingsWithRank
    ) {
        return new ReservationsAndWaitingsResponse(
            reservations.stream()
                .map(ReservationResponse::from)
                .toList(),
            waitingsWithRank
        );
    }
}
