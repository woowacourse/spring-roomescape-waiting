package roomescape.time.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.time.AvailableTime;

public record AvailableTimeResponse(
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        Boolean isAvailable
) {
    public static AvailableTimeResponse from(AvailableTime availableTime) {
        return new AvailableTimeResponse(availableTime.getTime(), availableTime.getIsAvailable());
    }
}
