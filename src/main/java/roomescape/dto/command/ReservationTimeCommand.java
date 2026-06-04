package roomescape.dto.command;

import java.time.LocalTime;

public record ReservationTimeCommand(
        LocalTime startAt
) {
}
