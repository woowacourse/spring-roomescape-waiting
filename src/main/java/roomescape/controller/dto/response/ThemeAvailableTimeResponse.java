package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.service.dto.TimeAvailability;

import java.time.LocalTime;

public record ThemeAvailableTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        Boolean isAvailable
) {
    public static ThemeAvailableTimeResponse from(TimeAvailability timeAvailability) {
        return new ThemeAvailableTimeResponse(
                timeAvailability.id(),
                timeAvailability.startAt(),
                timeAvailability.available()
        );
    }
}
