package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;

public class ReservationWaiting {
    public static final String PAST_WAITING_MESSAGE = "지난 예약에는 대기를 생성할 수 없습니다.";

    private final Long id;
    private final Reservation reservation;
    private final ReservationName name;
    private final LocalDateTime requestedAt;

    private ReservationWaiting(
            final Long id,
            final Reservation reservation,
            final String name,
            final LocalDateTime requestedAt
    ) {
        this.id = id;
        this.reservation = reservation;
        this.name = ReservationName.from(name);
        this.requestedAt = requestedAt;
        validateWaitable();
    }

    public static ReservationWaiting createNew(
            final Reservation reservation,
            final String name,
            final LocalDateTime requestedAt
    ) {
        return new ReservationWaiting(null, reservation, name, requestedAt);
    }

    public static ReservationWaiting of(
            final Long id,
            final Reservation reservation,
            final String name,
            final LocalDateTime requestedAt
    ) {
        return new ReservationWaiting(id, reservation, name, requestedAt);
    }

    public ReservationWaiting withId(final Long id) {
        return ReservationWaiting.of(id, this.reservation, this.name.value(), this.requestedAt);
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getName() {
        return name.value();
    }

    public boolean hasName(final String name) {
        return this.name.value().equals(ReservationName.from(name).value());
    }

    public Reservation toReservation(final LocalDateTime requestedAt) {
        return new Reservation(name.value(), reservation.getSlot(), requestedAt);
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    private void validateWaitable() {
        if (reservation.isPast(requestedAt)) {
            throw new IllegalArgumentException(PAST_WAITING_MESSAGE);
        }
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
}
