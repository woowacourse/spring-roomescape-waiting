package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.reservation.Reservation;

public class ReservationWaiting {
    private final Long id;
    private final Reservation reservation;
    private final String name;
    private final LocalDateTime requestAt;

    public ReservationWaiting(
            final Long id,
            final Reservation reservation,
            final String name,
            final LocalDateTime requestAt
    ) {
        this.id = id;
        this.reservation = reservation;
        this.name = name;
        this.requestAt = requestAt;
    }

    public static ReservationWaiting createNew(
            final Reservation reservation,
            final String name,
            final LocalDateTime requestAt
    ) {
        validateWaitable(reservation, requestAt);
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

    public boolean hasName(final String name) {
        return this.name.equals(name);
    }

    public LocalDateTime getRequestAt() {
        return requestAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ReservationWaiting)) {
            return false;
        }

        ReservationWaiting reservationWaiting = (ReservationWaiting) o;
        return id != null && Objects.equals(id, reservationWaiting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static void validateWaitable(final Reservation reservation, final LocalDateTime requestAt) {
        if (reservation.isPastAt(requestAt)) {
            throw new IllegalArgumentException("지난 예약에는 대기를 생성할 수 없습니다.");
        }
    }
}
