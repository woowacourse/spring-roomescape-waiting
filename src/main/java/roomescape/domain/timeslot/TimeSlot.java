package roomescape.domain.timeslot;

import java.time.LocalTime;
import java.util.Objects;

public class TimeSlot {

    private final Long id;
    private final LocalTime startAt;

    public TimeSlot(Long id, LocalTime startAt) {
        validateTime(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public TimeSlot(LocalTime startAt) {
        this(null, startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    private void validateTime(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시작 시간은 필수입니다.");
        }
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
