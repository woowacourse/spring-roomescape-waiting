package roomescape.domain.reservation;

import roomescape.exception.InvalidReservationException;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ReservationTime {
    private final static long NO_ID = 0;

    private final long id;
    private final LocalTime startAt;

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

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
