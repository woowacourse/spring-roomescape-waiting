package roomescape.time.service;

import java.time.LocalTime;

public record ReservationTimeCommand(LocalTime startAt) {
}
