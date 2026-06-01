package roomescape.reservationtime.application.dto;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeCreateCommand(
        LocalTime startAt
) {

    public ReservationTime toEntity() {
        return ReservationTime.builder()
                .startAt(startAt)
                .build();
    }
}
