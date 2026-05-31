package roomescape.domain;

import java.time.LocalTime;

import static roomescape.domain.exception.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.exception.DomainPreconditions.requireNonNull;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = requireNonNull(startAt, INVALID_INPUT, "시작 시각은 비어있을 수 없습니다.");
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
