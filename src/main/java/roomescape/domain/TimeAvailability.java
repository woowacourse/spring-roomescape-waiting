package roomescape.domain;

public class TimeAvailability {

    private final ReservationTime time;
    private final boolean available;

    public TimeAvailability(ReservationTime time, boolean available) {
        validateTime(time);

        this.time = time;
        this.available = available;
    }

    public ReservationTime getTime() {
        return time;
    }

    public boolean isAvailable() {
        return available;
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("time은 비어있을 수 없습니다.");
        }
    }
}
