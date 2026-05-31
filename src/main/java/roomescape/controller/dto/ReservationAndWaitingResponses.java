package roomescape.controller.dto;

import java.util.List;
import roomescape.service.dto.ReservationAndWaiting;

public record ReservationAndWaitingResponses(
        List<ReservationAndWaitingResponse> reservationAndWaitingResponses
) {

    public static ReservationAndWaitingResponses from(List<ReservationAndWaiting> reservationAndWaitings) {
        return new ReservationAndWaitingResponses(
                reservationAndWaitings.stream()
                        .map(ReservationAndWaitingResponse::from)
                        .toList()
        );
    }
}
