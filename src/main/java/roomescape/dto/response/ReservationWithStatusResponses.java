package roomescape.dto.response;

import java.util.List;
import java.util.Map;
import roomescape.domain.Reservation;

public record ReservationWithStatusResponses(
        List<ReservationResponse> reservations,
        List<WaitingReservationResponse> waitingReservations,
        boolean hasNext
) {
    public static ReservationWithStatusResponses of(
            List<Reservation> reservations,
            Map<Reservation, Integer> waitingReservations,
            boolean hasNext
    ) {
        return new ReservationWithStatusResponses(
                reservations.stream()
                        .map(ReservationResponse::from)
                        .toList(),
                waitingReservations.entrySet().stream()
                        .map(entry -> WaitingReservationResponse.from(entry.getKey(), entry.getValue()))
                        .toList(),
                hasNext
        );
    }
}
