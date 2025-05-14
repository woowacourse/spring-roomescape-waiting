package roomescape.domain.reservation.model.entity;

import java.time.LocalTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    @Builder
    public ReservationTime(Long id, LocalTime startAt) {
        validateNotNull(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime assignId(Long id) {
        return new ReservationTime(id, startAt);
    }

    private void validateNotNull(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("예약 시작시간은 null이 될 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        if (this.id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
