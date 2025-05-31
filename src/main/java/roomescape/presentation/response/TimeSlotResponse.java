package roomescape.presentation.response;

import java.time.LocalTime;
import java.util.List;
import roomescape.domain.timeslot.TimeSlot;

public record TimeSlotResponse(
        long id,
        LocalTime startAt
) {

    public static List<TimeSlotResponse> fromTimeSlots(
            final List<TimeSlot> timeSlots
    ) {
        return timeSlots.stream()
                .map(TimeSlotResponse::fromTimeSlot)
                .toList();
    }

    public static TimeSlotResponse fromTimeSlot(
            final TimeSlot timeSlot
    ) {
        return new TimeSlotResponse(
                timeSlot.id(),
                timeSlot.startAt()
        );
    }
}
