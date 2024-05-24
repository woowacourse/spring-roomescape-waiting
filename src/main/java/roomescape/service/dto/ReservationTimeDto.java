package roomescape.service.dto;

import roomescape.model.ReservationTime;

import java.time.LocalTime;

public class ReservationTimeDto {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTimeDto(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTimeDto(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime toReservationTime() {
        return new ReservationTime(this.id, this.startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
