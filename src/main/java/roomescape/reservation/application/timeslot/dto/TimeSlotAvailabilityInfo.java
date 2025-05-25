package roomescape.reservation.application.timeslot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record TimeSlotAvailabilityInfo(
        long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {
}
