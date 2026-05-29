package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.CustomInvalidDomainException;
import roomescape.exception.ErrorCode;

public class Wait {

    private final Long id;
    private final LocalDateTime createdAt;
    private final String name;
    private final LocalDate reservationDate;
    private final ReservationTime time;
    private final Theme theme;

    public Wait(Long id, LocalDateTime createAt, String name, LocalDate reservationDate, ReservationTime time,
                Theme theme) {
        validate(createAt, name, reservationDate, time, theme);
        this.id = id;
        this.createdAt = createAt;
        this.name = name;
        this.reservationDate = reservationDate;
        this.time = time;
        this.theme = theme;
    }

    public Wait(LocalDateTime createAt, String name, LocalDate reservationDate, ReservationTime time, Theme theme) {
        this(null, createAt, name, reservationDate, time, theme);
    }

    public static Wait of(Long id, Wait wait) {
        return new Wait(id, wait.createdAt, wait.name, wait.reservationDate, wait.time, wait.theme);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    private void validate(LocalDateTime createAt, String name, LocalDate date, ReservationTime time, Theme theme) {
        if (createAt == null) {
            throw new CustomInvalidDomainException(ErrorCode.NOT_ALLOW_DATE_TIME_NULL);
        }
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Wait wait = (Wait) object;
        return Objects.equals(id, wait.id) && Objects.equals(createdAt, wait.createdAt)
                && Objects.equals(name, wait.name) && Objects.equals(reservationDate,
                wait.reservationDate) && Objects.equals(time, wait.time) && Objects.equals(theme,
                wait.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, name, reservationDate, time, theme);
    }
}
