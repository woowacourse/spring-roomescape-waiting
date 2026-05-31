package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.AvailableTime;

public record AvailableReservationTimeResponse(long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt, boolean isAvailable) {
    public static AvailableReservationTimeResponse from(AvailableTime availableTime) {
        return new AvailableReservationTimeResponse(
                availableTime.time().getId(),
                availableTime.time().getStartAt(),
                availableTime.available()
        );
    }
}
