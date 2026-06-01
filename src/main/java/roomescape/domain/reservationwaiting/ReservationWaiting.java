package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import roomescape.domain.reservation.Reservation;

public class ReservationWaiting {
    private final Long id;
    private final Reservation reservation;
    private final String name;
    private final LocalDateTime requestedAt;

    public ReservationWaiting(Long id, Reservation reservation, String name, LocalDateTime requestedAt) {
        this.id = id;
        this.reservation = reservation;
        this.name = name;
        this.requestedAt = requestedAt;
    }

    public static ReservationWaiting createNew(final Reservation reservation, String name, LocalDateTime requestedAt) {
        return new ReservationWaiting(null, reservation, name, requestedAt);
    }

    public ReservationWaiting withId(final Long id) {
        return new ReservationWaiting(id, this.reservation, this.name, this.requestedAt);
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
