package roomescape.presentation.response;

import java.time.LocalTime;
import roomescape.domain.timeslot.TimeSlot;

public record TimeSlotResponse(
        long id,
        LocalTime startAt
) {

    public static TimeSlotResponse from(final TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.id(),
                timeSlot.startAt()
        );
    }
}
