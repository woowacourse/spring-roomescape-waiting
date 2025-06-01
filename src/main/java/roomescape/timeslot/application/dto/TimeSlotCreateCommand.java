package roomescape.timeslot.application.dto;

import java.time.LocalTime;
import roomescape.timeslot.domain.TimeSlot;

public record TimeSlotCreateCommand(LocalTime startAt) {

    public TimeSlot convertToEntity() {
        return new TimeSlot(startAt);
    }
}
