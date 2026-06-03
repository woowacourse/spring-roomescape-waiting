package roomescape.dto.reservationtime.response;

import java.util.List;
import roomescape.domain.ReservationTime;

public record ReservationTimeResponses(
        List<ReservationTimeResponse> times,
        boolean hasNext
) {
    public static ReservationTimeResponses of(List<ReservationTime> reservationTimes, boolean hasNext) {
        return new ReservationTimeResponses(
                reservationTimes.stream().map(ReservationTimeResponse::from).toList(),
                hasNext
        );
    }
}