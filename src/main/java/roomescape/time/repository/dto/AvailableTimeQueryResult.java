package roomescape.time.repository.dto;

import java.time.LocalTime;

public record AvailableTimeQueryResult(
        Long id,
        LocalTime startAt
) {
}
