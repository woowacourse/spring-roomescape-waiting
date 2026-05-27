package roomescape.time.repository.entity;

import java.time.LocalTime;

public class ReservationTimeEntity {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTimeEntity(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
