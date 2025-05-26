package roomescape.presentation.response;

import java.time.LocalTime;
import java.util.List;
import roomescape.domain.timeslot.AvailableTimeSlot;

public record AvailableTimeSlotResponse(
        long id,
        LocalTime startAt,
        Boolean alreadyBooked
) {
    public static List<AvailableTimeSlotResponse> fromAvailableTimeSlots(
            final List<AvailableTimeSlot> availableTimeSlots) {
        return availableTimeSlots.stream()
                .map(AvailableTimeSlotResponse::fromAvailableTimeSlot)
                .toList();
    }

    private static AvailableTimeSlotResponse fromAvailableTimeSlot(final AvailableTimeSlot availableTimeSlot) {
        return new AvailableTimeSlotResponse(
                availableTimeSlot.timeSlot().id(),
                availableTimeSlot.timeSlot().startAt(),
                availableTimeSlot.alreadyBooked()
        );
    }
}
