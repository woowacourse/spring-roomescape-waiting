package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.domain.TimeSlot;

public record TimeSlotResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt) {
    public static TimeSlotResponse from(TimeSlot timeSlot) {
        return new TimeSlotResponse(timeSlot.getId(), timeSlot.getStartAt());
    }
}
