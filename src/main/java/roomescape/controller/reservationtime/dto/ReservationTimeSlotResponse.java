package roomescape.controller.reservationtime.dto;

import java.time.LocalTime;

public record ReservationTimeSlotResponse(
        Long id,
        LocalTime startAt,
        ReservationTimeSlotStatus status,
        Long waitingId
) {

    public boolean reservable() {
        return status == ReservationTimeSlotStatus.RESERVABLE;
    }

    public boolean waitable() {
        return status == ReservationTimeSlotStatus.WAITABLE;
    }

    public boolean past() {
        return status == ReservationTimeSlotStatus.PAST;
    }

    public boolean waiting() {
        return waitingId != null;
    }
}
