package roomescape.feature.time.dto.command;

import java.time.LocalTime;

public record TimeCreateCommand(
    LocalTime startAt
) {
}
