package roomescape.reservationtime.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateReservationTimeIsNull(startAt);

        this.id = id;
        this.startAt = startAt;
    }

    protected ReservationTime() {
    }

    public static ReservationTime of(final Long id, final ReservationTime reservationTime) {
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private void validateReservationTimeIsNull(final LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약 시간 생성 시 시작 시간은 필수입니다.");
        }
    }

    public boolean isNotAfter(final LocalTime time) {
        return this.startAt.isBefore(time) || isSameStartAt(time);
    }

    public boolean isSameTo(final Long timeId) {
        return Objects.equals(this.id, timeId);
    }

    public boolean isSameStartAt(final LocalTime time) {
        return Objects.equals(this.startAt, time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
