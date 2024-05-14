package roomescape.reservation.model;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateTime(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateTime(final LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시간 정보는 공백을 입력할 수 없습니다.");
        }
    }

    public ReservationTime initializeIndex(final Long reservationId) {
        return new ReservationTime(reservationId, startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final ReservationTime that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}
