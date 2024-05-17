package roomescape.domain.dto;

import java.time.LocalTime;

public record AvailableTimeDto(
        Long id,
        LocalTime startAt,
        boolean isBooked
) {
}
