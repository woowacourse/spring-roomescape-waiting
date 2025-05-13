package roomescape.reservationTime.domain.dto;

import java.time.LocalTime;
import roomescape.reservationTime.domain.ReservationTime;

public record ReservationTimeRequestDto(LocalTime startAt) {

    public ReservationTime toEntity() {
        return new ReservationTime(startAt);
    }
}
