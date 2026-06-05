package roomescape.service.event;

import roomescape.domain.Reservation;

public record ReservationCanceledEvent(Long slotId) {

    public static ReservationCanceledEvent from(Reservation reservation) {
        return new ReservationCanceledEvent(reservation.getSlot().getId());
    }
}
