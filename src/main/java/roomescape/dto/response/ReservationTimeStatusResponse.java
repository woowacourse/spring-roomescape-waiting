package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationStatus;
import roomescape.dto.projection.ReservationTimeStatusProjection;

public record ReservationTimeStatusResponse(Long id, LocalTime startAt, ReservationStatus status) {
    public static ReservationTimeStatusResponse from(ReservationTimeStatusProjection reservationTimeStatus) {
        return new ReservationTimeStatusResponse(
                reservationTimeStatus.getTimeId(),
                reservationTimeStatus.getTimeStartAt(),
                reservationTimeStatus.getStatus()
        );
    }
}
