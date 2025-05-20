package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.domain.TimeSlot;

public record ReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt) {
    public static ReservationTimeResponse from(TimeSlot timeSlot) {
        return new ReservationTimeResponse(timeSlot.getId(), timeSlot.getStartAt());
    }
}
