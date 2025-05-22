package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.ArgumentNullException;

@Entity
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startAt;

    private TimeSlot(final Long id, final LocalTime startAt) {
        validateNull(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    protected TimeSlot() {
    }

    private void validateNull(LocalTime startAt) {
        if (startAt == null) {
            throw new ArgumentNullException("startAt");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeSlot that = (TimeSlot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private LocalTime startAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder startAt(LocalTime startAt) {
            this.startAt = startAt;
            return this;
        }

        public TimeSlot build() {
            return new TimeSlot(id, startAt);
        }
    }
}
