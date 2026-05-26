package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.theme.Theme;

public class Reservation {
    private final long id;
    private final ReservationName reservationName;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(long id, ReservationName reservationName, ReservationDate date, ReservationTime time,
                        Theme theme) {
        this.id = id;
        this.reservationName = Objects.requireNonNull(reservationName);
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
    }

    public static Reservation load(long id, ReservationName reservationName, ReservationDate date, ReservationTime time,
                                   Theme theme) {
        return new Reservation(id, reservationName, date, time, theme);
    }

    public static Reservation reserve(ReservationName reservationName, ReservationDate date, ReservationTime time,
                                      Theme theme, LocalDateTime now) {
        Objects.requireNonNull(now);
        Reservation reservation = new Reservation(0L, reservationName, date, time, theme);
        reservation.ensureNotPast(now);
        return reservation;
    }

    public void ensureNotPast(LocalDateTime now) {
        LocalDateTime requestDateTime = LocalDateTime.of(date.getDate(), time.getStartAt());

        if (requestDateTime.isBefore(now)) {
            throw new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    public boolean isSameName(String name) {
        return reservationName.isSame(name);
    }

    public long getId() {
        return id;
    }

    public ReservationName getName() {
        return reservationName;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
