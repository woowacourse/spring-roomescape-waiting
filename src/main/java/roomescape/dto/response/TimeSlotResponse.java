package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationTimeStatus;
import roomescape.domain.TimeSlot;

public record TimeSlotResponse(Long id, LocalTime startAt, ReservationTimeStatus status) {
    public static TimeSlotResponse from(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getTimeId(),
                timeSlot.getTimeStartAt(),
                timeSlot.getStatus()
        );
    }
}
