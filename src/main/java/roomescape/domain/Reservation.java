package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.DomainPreconditions;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        validate(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        this(null, name, date, time, theme);
    }

    public static Reservation of(Long id, Reservation reservation) {
        return new Reservation(id, reservation.name, reservation.date, reservation.time, reservation.theme);
    }

    private void validate(String name, LocalDate date, ReservationTime time, Theme theme) {
        DomainPreconditions.requireNonBlank(name, DomainErrorCode.INVALID_INPUT, "name");
        DomainPreconditions.requireNonNull(date, DomainErrorCode.INVALID_INPUT, "date");
        DomainPreconditions.requireNonNull(time, DomainErrorCode.INVALID_INPUT, "time");
        DomainPreconditions.requireNonNull(theme, DomainErrorCode.INVALID_INPUT, "theme");
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

    public boolean isReservedBy(String name) {
        return this.name.equals(name);
    }

    public boolean isPast(LocalDateTime now) {
        if (date.isBefore(now.toLocalDate())) {
            return true;
        }
        if (date.isAfter(now.toLocalDate())) {
            return false;
        }
        return time.isPast(now.toLocalTime());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name)
                && Objects.equals(date, that.date) && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, time, theme);
    }
}
