package roomescape.domain.reservationtime;

import java.time.LocalTime;

public record ReservationTimeSlot(
        Long id,
        LocalTime startAt,
        ReservationTimeSlotStatus status
) {

    public static ReservationTimeSlot of(
            final ReservationTime reservationTime,
            final ReservationTimeSlotStatus status
    ) {
        return new ReservationTimeSlot(reservationTime.getId(), reservationTime.getStartAt(), status);
    }

    public boolean reservable() {
        return status == ReservationTimeSlotStatus.RESERVABLE;
    }
}
