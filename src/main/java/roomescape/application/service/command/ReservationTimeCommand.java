package roomescape.application.service.command;

import java.time.LocalTime;

public record ReservationTimeCommand(
        LocalTime startAt
) {
}
