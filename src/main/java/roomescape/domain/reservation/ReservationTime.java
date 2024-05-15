package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;

@Entity
public class ReservationTime {
    private static final long NO_ID = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalTime startAt;

    public ReservationTime() {
    }

    public ReservationTime(long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(long id, ReservationTime reservationTime) {
        this(id, reservationTime.startAt);
    }

    public ReservationTime(long id, String time) {
        this.id = id;
        this.startAt = LocalTime.parse(time);
    }

    public ReservationTime(LocalTime time) {
        this(NO_ID, time);
    }

    public boolean isSame(ReservationTime other) {
        return startAt.equals(other.startAt);
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
