package roomescape.reservationtime.domain;

import jakarta.persistence.*;
import roomescape.exception.DomainValidationException;

import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "reservation_time")
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private LocalTime startAt;

    protected ReservationTime() {
    }

    public ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime generateWithPrimaryKey(ReservationTime reservationTime, Long newPrimaryKey) {
        return new ReservationTime(newPrimaryKey, reservationTime.startAt);
    }

    private void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new DomainValidationException("startAt(시작 시간)은 비어있을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        if (that.getId() == null || id == null) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
