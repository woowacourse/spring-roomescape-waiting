package roomescape.application.dto;

import java.time.LocalTime;
import roomescape.domain.dto.AvailableTimeDto;

public record AvailableTimeResponse(
        Long id,
        LocalTime startAt,
        boolean isBooked
) {

    public static AvailableTimeResponse from(AvailableTimeDto dto) {
        return new AvailableTimeResponse(dto.id(), dto.startAt(), dto.isBooked());
    }
}
