package roomescape.reservationtime.model;

import jakarta.persistence.Column;
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

    @Column(nullable = false, unique = true)
    private LocalTime startAt;

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateReservationTimeIsNull(startAt);

        this.id = id;
        this.startAt = startAt;
    }

    protected ReservationTime() {
    }

    private void validateReservationTimeIsNull(final LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약 시간 생성 시 시작 시간은 필수입니다.");
        }
    }

    public boolean isSameTo(final Long timeId) {
        return Objects.equals(this.id, timeId);
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
