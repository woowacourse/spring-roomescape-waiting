package roomescape.controller.dto.response;

import roomescape.domain.ReservationTime;

import java.util.List;

public record ReservationTimesResponse(
        List<ReservationTimeResponse> times
) {
    public static ReservationTimesResponse from(List<ReservationTime> times) {
        return new ReservationTimesResponse(times.stream()
                .map(ReservationTimeResponse::from).toList()
        );
    }
}
