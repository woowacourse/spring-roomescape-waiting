package roomescape.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public static ReservationTime from(long id, LocalTime startAt) {
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
