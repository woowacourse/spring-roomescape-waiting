package roomescape.reservationtime.dto;

import java.time.LocalTime;

public record ReservationTimeRequest(LocalTime startAt) {
}
