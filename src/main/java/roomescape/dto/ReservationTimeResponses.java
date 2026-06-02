package roomescape.dto;

import roomescape.domain.ReservationTime;

import java.util.List;

public record ReservationTimeResponses(
        List<ReservationTimeResponse> reservationTimes
) {
    public static ReservationTimeResponses from(List<ReservationTime> times) {
        return new ReservationTimeResponses(times.stream()
                .map(ReservationTimeResponse::from)
                .toList());
    }
}
