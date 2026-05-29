package roomescape.reservationwaiting;

import java.time.LocalDateTime;
import roomescape.reservation.Reservation;

public class ReservationWaiting {
    private final Long id;
    private final Reservation reservation;
    private final String name;
    private final LocalDateTime requestAt;

    public ReservationWaiting(Long id, Reservation reservation, String name, LocalDateTime requestAt) {
        this.id = id;
        this.reservation = reservation;
        this.name = name;
        this.requestAt = requestAt;
    }

    public static ReservationWaiting createNew(final Reservation reservation, String name, LocalDateTime requestAt) {
        return new ReservationWaiting(null, reservation, name, requestAt);
    }

    public ReservationWaiting withId(final Long id) {
        return new ReservationWaiting(id, this.reservation, this.name, this.requestAt);
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

    public LocalDateTime getRequestAt() {
        return requestAt;
    }
}
