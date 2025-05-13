package roomescape.domain.time;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime time) {
        this.id = Objects.requireNonNull(id, "id는 null일 수 없습니다.");
        this.startAt = Objects.requireNonNull(time, "startAt은 null일 수 없습니다.");
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
