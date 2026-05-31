package roomescape.domain;

import java.time.LocalTime;
import roomescape.domain.exception.InvalidInputException;

public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        if (startAt == null) {
            throw new InvalidInputException("예약 시간은 필수입니다.");
        }
        this.id = id;
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
