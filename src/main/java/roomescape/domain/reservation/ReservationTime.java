package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import roomescape.exception.InvalidReservationException;

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
        validate(time);
        this.id = id;
        this.startAt = LocalTime.parse(time);
    }

    public ReservationTime(String time) {
        validate(time);
        this.id = NO_ID;
        this.startAt = LocalTime.parse(time);
    }

    private void validate(String time) {
        try {
            LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new InvalidReservationException("올바르지 않은 시간입니다.");
        }
    }

    public boolean isSame(ReservationTime other){
        return startAt.equals(other.startAt);
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
