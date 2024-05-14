package roomescape.domain.dto;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record TimeSlotResponse(Long id, LocalTime startAt) {
    public static TimeSlotResponse from(ReservationTime reservationTime) {
        return new TimeSlotResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
