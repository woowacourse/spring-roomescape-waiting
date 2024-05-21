package roomescape.service.dto.response.reservationwait;

import java.util.List;
import roomescape.domain.reservationwait.ReservationWait;

public record ReservationWaitResponses(List<ReservationWaitResponse> reservationWaits) {

    public static ReservationWaitResponses from(List<ReservationWait> reservationWaits) {
        List<ReservationWaitResponse> responses = reservationWaits.stream()
                .map(ReservationWaitResponse::new)
                .toList();
        return new ReservationWaitResponses(responses);
    }
}
