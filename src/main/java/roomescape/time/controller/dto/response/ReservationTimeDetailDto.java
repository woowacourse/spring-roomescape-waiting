package roomescape.time.controller.dto.response;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record ReservationTimeDetailDto(
    Long id,
    LocalTime startAt,
    boolean isActive
) {

    public static ReservationTimeDetailDto from(ReservationTime reservationTime) {
        return new ReservationTimeDetailDto(reservationTime.getId(), reservationTime.getStartAt(),
            reservationTime.isActive());
    }
}
