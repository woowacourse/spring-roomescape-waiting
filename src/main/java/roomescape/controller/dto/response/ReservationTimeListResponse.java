package roomescape.controller.dto.response;

import java.util.List;
import roomescape.domain.ReservationTime;

public record ReservationTimeListResponse(
        List<ReservationTimeResponse> items
) {
    public static ReservationTimeListResponse from(List<ReservationTime> reservationTimes) {
        return new ReservationTimeListResponse(reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList()
        );
    }
}
