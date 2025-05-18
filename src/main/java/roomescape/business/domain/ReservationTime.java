package roomescape.business.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "reservation_times")
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(final Long id) {
        this.id = id;
        this.startAt = null;
    }

    public ReservationTime() {
    }

    private void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("startAt이 null 입니다.");
        }
    }

    public boolean isSameReservationTime(final ReservationTime reservationTime) {
        return id.equals(reservationTime.getId());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
