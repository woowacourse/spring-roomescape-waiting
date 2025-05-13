package roomescape.domain;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.InvalidReservationTimeException;

public class ReservationTime {

    private final Long id;
    private final LocalTime time;

    public ReservationTime(Long id, LocalTime time) {
        validate(time);
        this.id = id;
        this.time = time;
    }

    public ReservationTime(LocalTime time) {
        this(null, time);
    }

    private void validate(LocalTime time) {
        if (time == null) {
            throw new InvalidReservationTimeException("유효하지 않은 예약시간입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time);
    }
}
