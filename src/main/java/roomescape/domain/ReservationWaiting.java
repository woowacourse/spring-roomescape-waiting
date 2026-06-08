package roomescape.domain;

import java.time.LocalDateTime;

public record ReservationWaiting(Reservation reservation, long waitingNumber) {

    public Reservation promoteToReservation(LocalDateTime createdAt) {
        return reservation.withCreatedAt(createdAt);
    }
}
