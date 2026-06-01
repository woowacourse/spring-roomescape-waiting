package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.theme.Theme;

public class Reservation {
    private final Long id;
    private final ReservationName reservationName;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Status status;
    private final LocalDateTime dateTime;

    private Reservation(Long id, ReservationName reservationName, ReservationDate date, ReservationTime time,
                        Theme theme, Status status, LocalDateTime dateTime) {
        this.id = id;
        this.reservationName = Objects.requireNonNull(reservationName);
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
        this.status = Objects.requireNonNull(status);
        this.dateTime = Objects.requireNonNull(dateTime);
    }

    public static Reservation load(Long id, ReservationName reservationName, ReservationDate date, ReservationTime time,
                                   Theme theme, Status status, LocalDateTime dateTime) {
        return new Reservation(Objects.requireNonNull(id), reservationName, date, time, theme, status, dateTime);
    }

    public static Reservation reserve(
            ReservationName reservationName,
            ReservationDate date,
            ReservationTime time,
            Theme theme,
            Status status,
            LocalDateTime now
    ) {
        Objects.requireNonNull(now);
        Reservation reservation = new Reservation(null, reservationName, date, time, theme, status, now);
        reservation.ensureNotPast(now);
        return reservation;
    }

    public void ensureNotPast(LocalDateTime now) {
        LocalDateTime requestDateTime = LocalDateTime.of(date.getDate(), time.getStartAt());

        if (requestDateTime.isBefore(now)) {
            throw new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED);
        }
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

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Reservation that)) {
            return false;
        }

        if (id == null || that.id == null) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
