package roomescape.feature.time.domain;

import java.time.LocalTime;
import java.util.Objects;
import lombok.Getter;
import roomescape.global.domain.EntityStatus;

@Getter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Time other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
