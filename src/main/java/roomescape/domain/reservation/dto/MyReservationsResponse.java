package roomescape.domain.reservation.dto;

import java.util.List;
import roomescape.domain.reservation.ReservationSummary;

public record MyReservationsResponse(
        List<MyReservationResponse> reservations
) {

    public static MyReservationsResponse from(List<ReservationSummary> summaries) {
        return new MyReservationsResponse(summaries.stream()
                .map(MyReservationResponse::from)
                .toList()
        );
    }
}
