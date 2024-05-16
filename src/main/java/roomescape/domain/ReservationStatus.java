package roomescape.domain;

import java.util.Objects;

public class ReservationStatus {

    private final ReservationTime time;
    private final boolean isBooked;

    public ReservationStatus(ReservationTime time, boolean isBooked) {
        this.time = time;
        this.isBooked = isBooked;
    }

    public ReservationTime getTime() {
        return time;
    }

    public boolean isBooked() {
        return isBooked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationStatus that = (ReservationStatus) o;
        return isBooked == that.isBooked && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, isBooked);
    }
}
