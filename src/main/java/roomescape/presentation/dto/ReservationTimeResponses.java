package roomescape.presentation.dto;

import java.util.List;
import roomescape.domain.ReservationTime;

public record ReservationTimeResponses(
        List<ReservationTimeResponse> reservationTimes
) {
    public static ReservationTimeResponses from(List<ReservationTime> times) {
        List<ReservationTimeResponse> reservationTimeResponses = times.stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return new ReservationTimeResponses(reservationTimeResponses);
    }
}
