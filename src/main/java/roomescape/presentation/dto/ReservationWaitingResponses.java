package roomescape.presentation.dto;

import java.util.List;
import roomescape.domain.projection.ReservationWaitingWithOrder;

public record ReservationWaitingResponses(
        List<ReservationWaitingResponse> waitings
) {
    public static ReservationWaitingResponses from(List<ReservationWaitingWithOrder> waitings) {
        List<ReservationWaitingResponse> reservationWaitingResponses = waitings.stream()
                .map(ReservationWaitingResponse::from)
                .toList();
        return new ReservationWaitingResponses(reservationWaitingResponses);
    }
}
