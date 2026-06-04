package roomescape.reservation.domain;

import java.time.LocalTime;

public record ReservationCountResult(
    Long timeId,
    LocalTime startAt,
    Long waitingCount
) {

    public static ReservationCountResult of(
        long timeId,
        LocalTime startAt,
        long waitingCount
    ) {
        return new ReservationCountResult(
            timeId,
            startAt,
            waitingCount
        );
    }
}
