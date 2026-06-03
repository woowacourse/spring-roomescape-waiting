package roomescape.reservationtime.repository.entity;

import java.sql.Time;

public record ReservationTimeEntity(
        Long id,
        Time startAt
) {
}
