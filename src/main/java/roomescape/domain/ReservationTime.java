package roomescape.domain;

import java.time.LocalTime;
import roomescape.exception.ReservationTimeErrorCode;
import roomescape.exception.RoomEscapeException;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        validateStartTime(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime create(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime of(Long id, LocalTime startAt) {
        validateId(id);
        return new ReservationTime(id, startAt);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new IllegalStateException("ID는 필수값입니다.");
        }
        if (id < 1) {
            throw new IllegalStateException("ID는 1 이상의 숫자여야 합니다. (입력값: " + id + ")");
        }
    }

    private static void validateStartTime(LocalTime startAt) {
        if (startAt == null || startAt.getMinute() != 0) {
            throw new RoomEscapeException(ReservationTimeErrorCode.INVALID_TIME);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public String toString() {
        return "ReservationTime{" +
                "id=" + id +
                ", startAt=" + startAt +
                '}';
    }
}
