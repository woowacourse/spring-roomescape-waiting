package roomescape.domain.reservationtime;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("예약 시간을 비어있을 수 없습니다.");
        }
    }

    public static ReservationTime createNew(final LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime of(final Long id, final LocalTime startAt) {
        validateId(id);
        return new ReservationTime(id, startAt);
    }

    public ReservationTime withId(final Long id) {
        validateId(id);
        return new ReservationTime(id, this.startAt);
    }

    private static void validateId(final Long id){
        if (id == null) {
            throw new IllegalArgumentException("Id는 비어있을 수 없습니다.");
        }
    }

    public Long getId() {
        return this.id;
    }

    public LocalTime getStartAt() {
        return this.startAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ReservationTime)) {
            return false;
        }
        ReservationTime r = (ReservationTime) o;
        return Objects.equals(id, r.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
