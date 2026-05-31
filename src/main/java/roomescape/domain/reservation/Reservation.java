package roomescape.domain.reservation;

import roomescape.common.exception.ReservationErrorCode;
import roomescape.common.exception.RoomEscapeException;
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
    private final Integer rank;

    private Reservation(Long id,
                        ReservationName reservationName,
                        ReservationDate date, ReservationTime time,
                        Theme theme,
                        Status status, Integer rank) {
        this.id = id;
        this.reservationName = Objects.requireNonNull(reservationName);
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
        this.status = Objects.requireNonNull(status);
        this.rank = rank;
    }

    public static Reservation load(Long id,
                                   ReservationName reservationName,
                                   ReservationDate date, ReservationTime time,
                                   Theme theme, Status status, int rank) {
        return new Reservation(id, reservationName, date, time, theme, status, rank);
    }

    public static Reservation reserve(
            ReservationName reservationName,
            ReservationDate date, ReservationTime time,
            Theme theme,
            LocalDateTime now,
            Status status
    ) {
        Reservation reservation = new Reservation(null, reservationName, date, time, theme, status, null);
        reservation.ensureNotPast(now);
        return reservation;
    }

    private void ensureNotPast(LocalDateTime now) {
        LocalDateTime requestDateTime = LocalDateTime.of(date.getDate(), time.getStartAt());

        if (requestDateTime.isBefore(now)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    public boolean isSameName(String name) {
        return reservationName.isSame(name);
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
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

    public Integer getRank() {
        return rank;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        if (id == null || that.id == null) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
