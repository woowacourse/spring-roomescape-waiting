package roomescape.domain.time.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Time {

    private final Long id;
    private final LocalTime startAt;
    private final LocalDateTime deletedAt;

    private Time(Long id, LocalTime startAt, LocalDateTime deletedAt) {
        this.id = id;
        this.startAt = startAt;
        this.deletedAt = deletedAt;
    }

    public static Time create(LocalTime startAt) {
        return new Time(null, startAt, null);
    }

    public static Time reconstruct(Long id, LocalTime startAt, LocalDateTime deletedAt) {
        return new Time(id, startAt, deletedAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
