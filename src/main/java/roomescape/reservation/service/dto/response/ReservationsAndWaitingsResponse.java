package roomescape.reservation.service.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.wating.service.dto.response.WaitingResponse;

import java.util.List;

public record ReservationsAndWaitingsResponse(
        List<ReservationResponse> reservations,
        List<WaitingResponse> waitings
) {


    public static ReservationsAndWaitingsResponse from(final List<Reservation> reservations, final List<WaitingResponse> waitingsWithRank) {
        return new ReservationsAndWaitingsResponse(
                reservations.stream()
                        .map(ReservationResponse::from)
                        .toList(),
                waitingsWithRank
        );
    }
}
