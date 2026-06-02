package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.theme.Theme;

public class Reservation {
    private final long id;
    private final ReservationName name;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Status status;
    private final LocalDateTime createdAt;

    private Reservation(long id, ReservationName name, ReservationDate date, ReservationTime time,
                        Theme theme, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Reservation load(long id, ReservationName reservationName, ReservationDate date, ReservationTime time,
                                   Theme theme, Status status, LocalDateTime dateTime) {
        return new Reservation(id, reservationName, date, time, theme, status, dateTime);
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
        Reservation reservation = new Reservation(0L, reservationName, date, time, theme, status, now);
        reservation.ensureNotPast(now);
        return reservation;
    }

    public void ensureNotPast(LocalDateTime now) {
        LocalDateTime requestDateTime = LocalDateTime.of(date.getValue(), time.getStartAt());

        if (requestDateTime.isBefore(now)) {
            throw new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    public long getId() {
        return id;
    }

    public ReservationName getName() {
        return name;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(date, that.date)
                && Objects.equals(time, that.time) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, time, theme);
    }
}
