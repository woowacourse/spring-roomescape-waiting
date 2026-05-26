package roomescape.dto.reservation;

import java.util.List;
import roomescape.domain.Reservation;

public record ReservationResponses(
        List<ReservationResponse> reservations,
        boolean hasNext
) {
    public static ReservationResponses of(List<Reservation> reservations, boolean hasNext) {
        return new ReservationResponses(
                reservations.stream().map(ReservationResponse::from).toList(),
                hasNext
        );
    }
}
