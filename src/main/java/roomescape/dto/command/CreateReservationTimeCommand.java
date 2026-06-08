package roomescape.dto.command;

import java.time.LocalTime;

public record CreateReservationTimeCommand(
        LocalTime startAt
) {
}
