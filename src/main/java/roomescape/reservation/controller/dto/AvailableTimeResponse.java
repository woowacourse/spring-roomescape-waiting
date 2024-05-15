package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.AvailableTime;

import java.time.LocalTime;

public record AvailableTimeResponse(long timeId, LocalTime startAt, boolean alreadyBooked) {
    public static AvailableTimeResponse from(AvailableTime availableTime) {
        return new AvailableTimeResponse(availableTime.getTimeId(), availableTime.getStartAt(),
                availableTime.isAlreadyBooked());
    }
}
