package roomescape.controller.dto.response;

import java.time.LocalTime;
import roomescape.domain.AvailableTime;

public record AvailableReservationTimeResponse(long id, LocalTime startAt, boolean isAvailable) {
    public static AvailableReservationTimeResponse from(AvailableTime availableTime) {
        return new AvailableReservationTimeResponse(
                availableTime.time().getId(),
                availableTime.time().getStartAt(),
                availableTime.available()
        );
    }
}
