package roomescape.timeslot.application.dto;

import java.time.LocalTime;
import roomescape.timeslot.domain.TimeSlot;

public record TimeSlotInfo(long id, LocalTime startAt) {

    public TimeSlotInfo(final TimeSlot timeSlot) {
        this(timeSlot.id(), timeSlot.startAt());
    }
}
