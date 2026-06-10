package roomescape.domain.reservation;

import java.time.LocalTime;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.requireNonNull;

public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = requireNonNull(startAt, INVALID_INPUT, "시작 시간을 비어있을 수 없습니다.");
    }

    public static ReservationTime load(Long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public static ReservationTime create(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public ReservationTime withId(Long id) {
        return new ReservationTime(id, this.getStartAt());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
