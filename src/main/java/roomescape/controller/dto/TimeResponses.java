package roomescape.controller.dto;

import java.util.List;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.service.dto.AvailableTimeSlot;

public record TimeResponses(
        List<TimeResponse> timeResponses
) {

    public static TimeResponses from(List<TimeSlot> timeSlots) {
        return new TimeResponses(
                timeSlots.stream()
                        .map(TimeResponse::from)
                        .toList()
        );
    }

    public static TimeResponses fromAvailable(List<AvailableTimeSlot> availableSlots) {
        return new TimeResponses(
                availableSlots.stream()
                        .map(TimeResponse::from)
                        .toList()
        );
    }
}
