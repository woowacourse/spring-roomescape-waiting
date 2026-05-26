package roomescape.dto.response;

import java.util.List;
import roomescape.domain.ReservationTime;

public record ReservationTimeResponse(
        Long id,
        String startAt
) {
    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt().toString()
        );
    }

    public static List<ReservationTimeResponse> fromAll(List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }
}
