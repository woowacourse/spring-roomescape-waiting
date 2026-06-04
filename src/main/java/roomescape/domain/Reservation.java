package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.exception.CustomInvalidDomainException;
import roomescape.exception.ErrorCode;

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
        validate(name, date, time, theme);
        this.id = null;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation withId(Long id, Reservation reservation) {
        return new Reservation(id, reservation.name, reservation.date, reservation.time, reservation.theme);
    }

    private void validate(String name, LocalDate date, ReservationTime time, Theme theme) {
        if (name == null || name.isBlank()) {
            throw new CustomInvalidDomainException(ErrorCode.NOT_ALLOW_NAME_NULL);
        }
        if (date == null) {
            throw new CustomInvalidDomainException(ErrorCode.NOT_ALLOW_DATE_NULL);
        }
        if (time == null) {
            throw new CustomInvalidDomainException(ErrorCode.NOT_ALLOW_TIME_NULL);
        }
        if (theme == null) {
            throw new CustomInvalidDomainException(ErrorCode.NOT_ALLOW_THEME_NULL);
        }
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
        Reservation that = (Reservation) object;
        return Objects.equals(name, that.name) && Objects.equals(date, that.date)
                && Objects.equals(time, that.time) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date, time, theme);
    }
}
