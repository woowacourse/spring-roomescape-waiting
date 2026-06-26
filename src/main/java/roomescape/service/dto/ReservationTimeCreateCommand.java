package roomescape.service.dto;

import java.time.LocalTime;

public record ReservationTimeCreateCommand(LocalTime startAt) {
}
