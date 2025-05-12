package roomescape.reservation.model;

import roomescape.global.exception.AlreadyEntityException;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime toEntity(Long id) {
        if (this.id == null) {
            return new ReservationTime(id, startAt);
        }
        throw new AlreadyEntityException("해당 예약시간은 이미 엔티티화 된 상태입니다.");
    }

    public boolean isBefore(LocalTime time) {
        return startAt.isBefore(time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
