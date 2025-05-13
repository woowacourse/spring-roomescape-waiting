package roomescape.entity;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.custom.InvalidReservationException;

public class ReservationSlot {

    private final Long id;
    private final LocalTime time;
    private final boolean reserved;

    public ReservationSlot(Long id, LocalTime time, boolean reserved) {
        validate(id, time);
        this.id = id;
        this.time = time;
        this.reserved = reserved;
    }

    private void validate(Long id, LocalTime time) {
        if (id == null) {
            throw new InvalidReservationException("아이디는 비어있을 수 없습니다.");
        }
        if (time == null) {
            throw new InvalidReservationException("시간은 비어있을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getTime() {
        return time;
    }

    public boolean isReserved() {
        return reserved;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationSlot that = (ReservationSlot) o;
        return reserved == that.reserved && Objects.equals(id, that.id) && Objects.equals(time,
                that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time, reserved);
    }
}

