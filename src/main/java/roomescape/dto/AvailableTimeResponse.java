package roomescape.dto;

import java.time.LocalTime;
import roomescape.domain.dto.AvailableTimeDto;

public record AvailableTimeResponse(long id, LocalTime startAt, boolean isBooked) {
    public static AvailableTimeResponse from(AvailableTimeDto dto) {
        return new AvailableTimeResponse(dto.id(), dto.startAt(), dto.isBooked());
    }
}
