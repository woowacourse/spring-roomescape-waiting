package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.domain.ReservationSlot;

public record ReservationTimeSlotResponse(
        Long id,
        LocalTime time,
        boolean alreadyBooked
) {
    public static ReservationTimeSlotResponse from(ReservationSlot reservationSlot) {
        return new ReservationTimeSlotResponse(
                reservationSlot.getId(),
                reservationSlot.getTime(),
                reservationSlot.isReserved()
        );
    }
}
