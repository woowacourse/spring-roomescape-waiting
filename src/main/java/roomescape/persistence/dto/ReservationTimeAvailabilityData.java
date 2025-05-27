package roomescape.persistence.dto;

import java.time.LocalTime;

public record ReservationTimeAvailabilityData (
        Long id,
        LocalTime startAt,
        boolean booked
){
}
