package roomescape.feature.time.domain;

import java.time.LocalTime;
import roomescape.global.domain.EntityStatus;

public class Time {

    private final Long id;
    private final LocalTime startAt;
    private final EntityStatus status;

    private Time(Long id, LocalTime startAt, EntityStatus status) {
        this.id = id;
        this.startAt = startAt;
        this.status = status;
    }

    public static Time create(LocalTime startAt) {
        return new Time(null, startAt, EntityStatus.ACTIVE);
    }

    public static Time reconstruct(Long id, LocalTime startAt, EntityStatus status) {
        return new Time(id, startAt, status);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public EntityStatus getStatus() {
        return status;
    }
}
