package roomescape.presentation.response;

import java.time.LocalTime;
import java.util.List;
import roomescape.domain.timeslot.TimeSlotBookStatus;

public record AvailableTimeSlotResponse(
        long id,
        LocalTime startAt,
        Boolean alreadyBooked
) {

    public static AvailableTimeSlotResponse from(final TimeSlotBookStatus timeSlotBookStatus) {
        return new AvailableTimeSlotResponse(
                timeSlotBookStatus.timeSlot().id(),
                timeSlotBookStatus.timeSlot().startAt(),
                timeSlotBookStatus.alreadyBooked()
        );
    }

    public static List<AvailableTimeSlotResponse> from(final List<TimeSlotBookStatus> timeSlotAvailiabilities) {
        return timeSlotAvailiabilities.stream()
                .map(AvailableTimeSlotResponse::from)
                .toList();
    }
}
