package roomescape.domain.time.dto;

import roomescape.domain.time.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeAddRequest(LocalTime startAt) {
    public ReservationTime toEntity() {
        return new ReservationTime(null, startAt);
    }
}
