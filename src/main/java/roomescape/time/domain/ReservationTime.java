package roomescape.time.domain;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.exception.ReservationTimeNotHourlyUnitException;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        validateHourlyUnit(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateHourlyUnit(LocalTime startAt) {
        if (startAt.getMinute() != 0) {
            throw new ReservationTimeNotHourlyUnitException("예약 시각은 정각 단위여야 합니다");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
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
        return Objects.equals(id, that.id) && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}
