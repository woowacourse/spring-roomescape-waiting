package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class ReservationWaiting {
    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private final Reservation reservation;
    private final int order;

    public ReservationWaiting(
            Long id,
            String name,
            LocalDateTime createdAt,
            Reservation reservation,
            int order
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.reservation = reservation;
        this.order = order;
    }

    public ReservationWaiting(
            String name,
            LocalDateTime createdAt,
            Reservation reservation
    ) {
        this(null, name, createdAt, reservation, 0);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWaiting that = (ReservationWaiting) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
