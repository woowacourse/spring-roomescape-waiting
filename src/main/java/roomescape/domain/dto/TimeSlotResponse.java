package roomescape.domain.dto;

import roomescape.domain.TimeSlot;

import java.time.LocalTime;

public record TimeSlotResponse(Long id, LocalTime startAt) {
    public static TimeSlotResponse from(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartAt()
        );
    }
}
