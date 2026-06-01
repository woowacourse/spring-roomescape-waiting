package roomescape.domain.reservation;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        Objects.requireNonNull(startAt, "시작 시간은 필수입니다.");

        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime from(Long id, LocalTime startAt) {
        Objects.requireNonNull(id, "조회 및 복원시 ReservationTime의 id는 필수입니다.");
        return new ReservationTime(id, startAt);
    }

    public static ReservationTime create(LocalTime startAt) {
        return new ReservationTime(null, startAt);
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
        ReservationTime time = (ReservationTime) o;
        return Objects.equals(id, time.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
