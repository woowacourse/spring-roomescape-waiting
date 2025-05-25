package roomescape.reservation.application.timeslot.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.timeslot.TimeSlot;

public record TimeSlotInfo(long id, LocalTime startAt) {

    public TimeSlotInfo(final TimeSlot timeSlot) {
        this(timeSlot.id(), timeSlot.startAt());
    }
}
