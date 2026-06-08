package roomescape.dto;

import roomescape.domain.WaitingWithOrder;

import java.util.List;

public record ReservationWaitingResponses(
        List<ReservationWaitingResponse> waitings
) {
    public static ReservationWaitingResponses from(List<WaitingWithOrder> waitings) {
        return new ReservationWaitingResponses(waitings.stream()
                .map(ReservationWaitingResponse::from)
                .toList());
    }
}
