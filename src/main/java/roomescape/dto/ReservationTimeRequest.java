package roomescape.dto;

import java.time.LocalTime;

import roomescape.entity.ReservationTime;

public record ReservationTimeRequest(LocalTime startAt) {
    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
