package roomescape.reservation.ui.timeslot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.application.timeslot.dto.TimeSlotInfo;

public record TimeSlotResponse(long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public TimeSlotResponse(final TimeSlotInfo timeSlotInfo) {
        this(timeSlotInfo.id(), timeSlotInfo.startAt());
    }
}
