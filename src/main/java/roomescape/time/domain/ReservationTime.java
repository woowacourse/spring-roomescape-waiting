package roomescape.time.domain;

import java.time.LocalTime;

public record ReservationTime(
        Long id,
        LocalTime startAt
) {
    public static ReservationTime of(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }
}
