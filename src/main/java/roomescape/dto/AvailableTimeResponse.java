package roomescape.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.dto.AvailableTimeDto;

public record AvailableTimeResponse(
        long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        boolean isBooked
) {
    public static AvailableTimeResponse from(AvailableTimeDto dto) {
        return new AvailableTimeResponse(dto.id(), dto.startAt(), dto.isBooked());
    }
}
