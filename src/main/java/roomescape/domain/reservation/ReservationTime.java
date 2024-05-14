package roomescape.domain.reservation;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class ReservationTime {

    private static final int AVAILABLE_TIME_UNIT = 10;

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(final String startAt) {
        this(null, startAt);
    }

    public ReservationTime(final Long id, final String startAt) {
        this(id, convertToLocalTime(startAt));
    }

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateTimeUnit(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private static LocalTime convertToLocalTime(final String time) {
        if (time == null || time.isEmpty()) {
            throw new IllegalArgumentException("예약 시간이 비어 있습니다.");
        }
        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("유효하지 않은 예약 시간입니다.");
        }
    }

    private void validateTimeUnit(final LocalTime time) {
        if (time.getMinute() % AVAILABLE_TIME_UNIT != 0) {
            throw new IllegalArgumentException("예약 시간은 " + AVAILABLE_TIME_UNIT + "분 단위로 등록할 수 있습니다.");
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startAt);
    }
}
