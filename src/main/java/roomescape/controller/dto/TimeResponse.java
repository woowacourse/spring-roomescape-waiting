package roomescape.controller.dto;

import java.time.LocalTime;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.service.dto.AvailableTimeSlot;

public record TimeResponse(
        long id,
        LocalTime startAt,
        boolean isAvailable
) {

    public static TimeResponse from(TimeSlot timeSlot) {
        return new TimeResponse(timeSlot.getId(), timeSlot.getStartAt(), true);
    }

    public static TimeResponse from(AvailableTimeSlot availableTimeSlot) {
        return new TimeResponse(
                availableTimeSlot.timeSlot().getId(),
                availableTimeSlot.timeSlot().getStartAt(),
                availableTimeSlot.isAvailable()
        );
    }
}
