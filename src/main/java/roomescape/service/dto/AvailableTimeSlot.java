package roomescape.service.dto;

import roomescape.domain.TimeSlot;

public record AvailableTimeSlot(TimeSlot timeSlot, boolean isAvailable) {
}
