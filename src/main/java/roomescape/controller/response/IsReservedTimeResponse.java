package roomescape.controller.response;

import java.time.LocalTime;
import java.util.Objects;

public class IsReservedTimeResponse {

    private final long timeId;
    private final LocalTime startAt;
    private boolean alreadyBooked;

    public IsReservedTimeResponse(long timeId, LocalTime startAt, boolean alreadyBooked) {
        this.timeId = timeId;
        this.startAt = startAt;
        this.alreadyBooked = alreadyBooked;
    }

    public long getTimeId() {
        return timeId;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean getAlreadyBooked() {
        return alreadyBooked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IsReservedTimeResponse that = (IsReservedTimeResponse) o;
        return timeId == that.timeId && alreadyBooked == that.alreadyBooked && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeId, startAt, alreadyBooked);
    }

    @Override
    public String toString() {
        return "MemberReservationTimeResponse{" +
                "timeId=" + timeId +
                ", startAt=" + startAt +
                ", alreadyBooked=" + alreadyBooked +
                '}';
    }
}
