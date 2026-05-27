package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTimeStatus;

public record ReservationTimeStatusResponse(Long id, LocalTime startAt, ReservationStatus status) {
    public static ReservationTimeStatusResponse from(ReservationTimeStatus reservationTimeStatus) {
        return new ReservationTimeStatusResponse(
                reservationTimeStatus.getTimeId(),
                reservationTimeStatus.getTimeStartAt(),
                reservationTimeStatus.getStatus()
        );
    }
}
