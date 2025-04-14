package roomescape.time.domain;

import java.time.LocalTime;
import roomescape.time.domain.exception.ReservationTimeNullException;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime time) {
        validateNull(time);
        this.id = id;
        this.startAt = time;
    }

    public static ReservationTime create(LocalTime time) {
        return new ReservationTime(null, time);
    }

    private void validateNull(LocalTime startAt) {
        if (startAt == null) {
            throw new ReservationTimeNullException("[ERROR] 시간은 비어있을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
