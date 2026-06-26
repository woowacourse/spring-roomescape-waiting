package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "time_slot")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalTime startAt;

    protected TimeSlot() {
    }

    public TimeSlot(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public static TimeSlot transientOf(LocalTime startAt) {
        return new TimeSlot(null, startAt);
    }

    public TimeSlot changeTime(LocalTime startAt) {
        return new TimeSlot(id, Optional.ofNullable(startAt).orElse(this.startAt));
    }

    private void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시작 시간은 필수입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TimeSlot timeSlot)) {
            return false;
        }
        return Objects.equals(id, timeSlot.id) && Objects.equals(startAt, timeSlot.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}
