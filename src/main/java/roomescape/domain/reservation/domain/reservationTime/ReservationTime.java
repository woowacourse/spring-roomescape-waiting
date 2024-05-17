package roomescape.domain.reservation.domain.reservationTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import roomescape.global.exception.EscapeApplicationException;

import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "reservation_time")
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    public ReservationTime() {

    }

    public ReservationTime(Long id, LocalTime startAt) {
        validateNonNull(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateNonNull(LocalTime startAt) {
        if (startAt == null) {
            throw new EscapeApplicationException("예약 가능 시각은 null일 수 없습니다");
        }
    }

    public Long getId() {
        return id;
    }

    @JsonFormat(pattern = "HH:mm")
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

    @Override
    public String toString() {
        return "ReservationTime{" +
                "id=" + id +
                ", startAt=" + startAt +
                '}';
    }
}
