package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.domain.ReservationException;

public class Reservation {

    private static final int RESERVATION_CHANGE_DEADLINE_PASSED = 1;

    private Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public static Reservation createFutureReservation(String name, LocalDate date,
                                                      ReservationTime time, Theme theme, LocalDateTime now) {
        validateNotPastDateTime(date, time, now);
        return new Reservation(null, name, date, time, theme);
    }

    private static void validateNotPastDateTime(LocalDate date, ReservationTime time, LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new ReservationException(ReservationErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        this(null, name, date, time, theme);
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation createWithId(long id) {
        return new Reservation(id, this.name, this.date, this.time, this.theme);
    }

    public boolean isNotModifiableAt(LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(
                date,
                time.getStartAt()
        );
        LocalDateTime cancelDeadline = reservationDateTime.minusDays(RESERVATION_CHANGE_DEADLINE_PASSED);
        return now.isAfter(cancelDeadline);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Reservation reservation = (Reservation) object;
        if (id != null && reservation.id != null) {
            return Objects.equals(id, reservation.id);
        }
        return Objects.equals(name, reservation.name)
                && Objects.equals(date, reservation.date) && Objects.equals(time, reservation.time)
                && Objects.equals(theme, reservation.theme);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, date, time, theme);
    }
}
