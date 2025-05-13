package roomescape.time.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public final class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime() {

    }

    private void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시간을 입력해야 합니다.");
        }
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isBeforeOrEqual(final LocalTime other) {
        return startAt.isBefore(other) || startAt.equals(other);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
