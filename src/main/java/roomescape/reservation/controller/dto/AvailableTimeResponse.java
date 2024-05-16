package roomescape.reservation.controller.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.AvailableTime;

public record AvailableTimeResponse(long timeId, LocalTime startAt, boolean alreadyBooked) {
    public static AvailableTimeResponse from(AvailableTime availableTime) {
        return new AvailableTimeResponse(availableTime.getTimeId(), availableTime.getStartAt(),
                availableTime.isAlreadyBooked());
    }
}
