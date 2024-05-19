package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.exception.RoomescapeException;

import java.time.LocalTime;

import static roomescape.exception.ExceptionType.EMPTY_TIME;

@Entity
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startAt;

    protected ReservationTime() {
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, LocalTime startAt) {
        if (startAt == null) {
            throw new RoomescapeException(EMPTY_TIME);
        }
        this.id = id;
        this.startAt = startAt;
    }

    public boolean isIdOf(long id) {
        return this.id == id;
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
