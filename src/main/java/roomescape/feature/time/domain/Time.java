package roomescape.feature.time.domain;

import java.time.LocalTime;

public class Time {

    private final Long id;
    private final LocalTime startAt;
    private final TimeStatus status;

    private Time(Long id, LocalTime startAt, TimeStatus status) {
        this.id = id;
        this.startAt = startAt;
        this.status = status;
    }

    public static Time create(LocalTime startAt) {
        return new Time(null, startAt, TimeStatus.ACTIVE);
    }

    public static Time reconstruct(Long id, LocalTime startAt, TimeStatus status) {
        return new Time(id, startAt, status);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public TimeStatus getStatus() {
        return status;
    }
}
