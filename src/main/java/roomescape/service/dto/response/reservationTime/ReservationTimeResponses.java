package roomescape.service.dto.response.reservationTime;

import java.util.List;
import roomescape.domain.ReservationTime;

public record ReservationTimeResponses(List<ReservationTimeResponse> reservationTimes) {

    public static ReservationTimeResponses from(List<ReservationTime> reservationTimes) {
        List<ReservationTimeResponse> responses = reservationTimes.stream()
                .map(ReservationTimeResponse::new)
                .toList();
        return new ReservationTimeResponses(responses);
    }
}
