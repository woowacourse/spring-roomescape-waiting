package roomescape.dto;

import java.util.List;
import roomescape.domain.WaitingWithOrder;

public record ReservationWaitingResponses(
        List<ReservationWaitingResponse> waitings
) {
    public static ReservationWaitingResponses from(List<WaitingWithOrder> waitings) {
        return new ReservationWaitingResponses(waitings.stream()
                .map(ReservationWaitingResponse::from)
                .toList());
    }
}
