package roomescape.time.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.exception.IllegalRequestException;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    protected ReservationTime() {
    }

    private void validate(LocalTime startAt) {
        validateNotNull(startAt);
        validateHourlyUnit(startAt);
    }

    private void validateNotNull(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalRequestException("예약 가능 시각은 필수 입니다");
        }
    }

    private void validateHourlyUnit(LocalTime startAt) {
        if (startAt.getMinute() != 0) {
            throw new IllegalRequestException("예약 시각은 정각 단위여야 합니다");
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
        return Objects.equals(id, that.id) && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}
