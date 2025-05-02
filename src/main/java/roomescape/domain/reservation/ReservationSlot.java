package roomescape.domain.reservation;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationSlot {

    private final Long id;
    private final LocalTime time;
    private final boolean reserved;

    public ReservationSlot(Long id, LocalTime time, boolean reserved) {
        this.id = id;
        this.time = time;
        this.reserved = reserved;
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

