package roomescape.time.service.dto;

import java.time.LocalTime;

public record ReservationTimeCommand(LocalTime startAt) {
}
