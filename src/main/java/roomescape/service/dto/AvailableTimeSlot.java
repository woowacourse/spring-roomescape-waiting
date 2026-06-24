package roomescape.service.dto;

import roomescape.domain.timeslot.TimeSlot;

public record AvailableTimeSlot(
        TimeSlot timeSlot,
        boolean isAvailable
) {
}
