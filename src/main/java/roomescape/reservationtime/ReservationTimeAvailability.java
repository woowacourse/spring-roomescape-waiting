package roomescape.reservationtime;

import roomescape.reservation.ReservationStatus;

public record ReservationTimeAvailability(
        ReservationTime reservationTime,
        Long reservationId,
        ReservationStatus reservationStatus
) {
    public boolean isAvailable() {
        return reservationId == null;
    }

    public boolean isWaitable() {
        return reservationStatus == ReservationStatus.CONFIRMED;
    }
}
