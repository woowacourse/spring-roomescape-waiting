package roomescape.domain.reservation;

import java.time.LocalTime;

public record ReservationCountResult(
        Long slotId,
        Long timeId,
        LocalTime startAt,
        Long waitingCount
) {

    public static ReservationCountResult of(
            long slotId,
            long timeId,
            LocalTime startAt,
            long waitingCount
    ) {
        return new ReservationCountResult(
                slotId,
                timeId,
                startAt,
                waitingCount
        );
    }
}
