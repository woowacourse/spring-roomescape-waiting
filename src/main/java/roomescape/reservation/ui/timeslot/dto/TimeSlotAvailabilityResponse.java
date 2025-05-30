package roomescape.reservation.ui.timeslot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.application.timeslot.dto.TimeSlotAvailabilityInfo;

public record TimeSlotAvailabilityResponse(
        long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {

    public TimeSlotAvailabilityResponse(final TimeSlotAvailabilityInfo timeSlotAvailabilityInfo) {
        this(timeSlotAvailabilityInfo.id(), timeSlotAvailabilityInfo.startAt(), timeSlotAvailabilityInfo.alreadyBooked());
    }
}
