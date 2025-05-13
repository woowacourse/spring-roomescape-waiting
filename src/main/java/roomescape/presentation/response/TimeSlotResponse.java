package roomescape.presentation.response;

import java.time.LocalTime;
import java.util.List;
import roomescape.domain.TimeSlot;

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

    public static List<TimeSlotResponse> from(final List<TimeSlot> timeSlots) {
        return timeSlots.stream()
            .map(TimeSlotResponse::from)
            .toList();
    }
}
