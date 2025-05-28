package roomescape.reservation.domain;

import java.time.LocalTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class ReservationTime {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id", nullable = false))
    private ReservationTimeId id;

    @Column(nullable = false)
    private LocalTime startAt;

    protected ReservationTime() {}

    public ReservationTime(final Long id, final LocalTime startAt) {
        this.id = new ReservationTimeId(id);
        this.startAt = startAt;
        validateReservationTime();
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public void validateReservationTime() {
        if (startAt == null) {
            throw new IllegalArgumentException("startAt cannot be null");
        }
    }

    public Long getId() {
        return id.getValue();
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
