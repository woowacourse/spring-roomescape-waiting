package roomescape.domain.reservation;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationSlot {

    private final Long reservationId;
    private final LocalTime time;
    private final boolean reserved;

    public ReservationSlot(Long reservationId, LocalTime time, boolean reserved) {
        this.reservationId = reservationId;
        this.time = time;
        this.reserved = reserved;
    }

    public Long getReservationId() {
        return reservationId;
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
        return reserved == that.reserved && Objects.equals(reservationId, that.reservationId) && Objects.equals(time,
                that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, time, reserved);
    }
}

