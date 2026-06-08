package roomescape.reservationtime.domain;

import java.time.LocalTime;

public record ReservationTime(Long id, LocalTime startAt) {
}
