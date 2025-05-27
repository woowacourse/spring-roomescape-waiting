package roomescape.domain.reservationtime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.reservation.InvalidReservationTimeException;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private LocalTime time;

    protected ReservationTime() {

    }

    public ReservationTime(Long id, LocalTime time) {
        validate(time);
        this.id = id;
        this.time = time;
    }

    private void validate(LocalTime time) {
        if (time == null) {
            throw new InvalidReservationTimeException("유효하지 않은 예약시간입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time);
    }
}
