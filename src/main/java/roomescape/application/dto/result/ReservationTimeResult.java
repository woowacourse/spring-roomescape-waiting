package roomescape.application.dto.result;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public class ReservationTimeResult {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTimeResult(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTimeResult from(ReservationTime time) {
        return new ReservationTimeResult(time.getId(), time.getStartAt());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
