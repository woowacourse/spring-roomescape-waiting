package roomescape.time;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.common.DomainAssert;
import roomescape.common.exception.InvalidInputException;

public class Time {
    private final Long id;
    private final LocalTime startAt;

    public Time(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public Time(LocalTime startAt) {
        this(null, startAt);
    }

    private void validate(LocalTime startAt) {
        DomainAssert.notNull(startAt, "시작 시간은 비어 있을 수 없습니다.");
        if (startAt.isBefore(LocalTime.of(10, 0)) || startAt.isAfter(LocalTime.of(22, 0))) {
            throw new InvalidInputException("영업 시간은 10시부터 22시 사이입니다.");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Time time)) {
            return false;
        }

        return id != null && Objects.equals(id, time.id);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
