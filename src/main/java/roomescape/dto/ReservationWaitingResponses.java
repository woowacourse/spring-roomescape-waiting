package roomescape.dto;

import java.util.List;
import roomescape.domain.ReservationWaiting;

public record ReservationWaitingResponses(
        List<ReservationWaitingResponse> waitings
) {
    public static ReservationWaitingResponses from(List<ReservationWaiting> waitings) {
        return new ReservationWaitingResponses(waitings.stream()
                .map(ReservationWaitingResponse::from)
                .toList());
    }
}
