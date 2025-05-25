package roomescape.reservation.application.timeslot.dto;

import java.time.LocalTime;
import roomescape.reservation.domain.timeslot.TimeSlot;

public record TimeSlotCreateCommand(LocalTime startAt) {

    public TimeSlot convertToEntity() {
        return new TimeSlot(startAt);
    }
}
