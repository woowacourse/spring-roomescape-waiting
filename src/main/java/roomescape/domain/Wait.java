package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.DomainPreconditions;
import roomescape.domain.exception.RoomEscapeException;

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

    public void validateDeletable(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(DomainErrorCode.PAST_RESERVATION_DELETE);
        }
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

    public boolean isWaitedBy(Wait other) {
        return this.name.equals(other.name);
    }

    private boolean isPast(LocalDateTime now) {
        if (reservationDate.isBefore(now.toLocalDate())) {
            return true;
        }
        if (reservationDate.isAfter(now.toLocalDate())) {
            return false;
        }
        return time.isPast(now.toLocalTime());
    }

    private void validate(LocalDateTime createAt, String name, LocalDate date, ReservationTime time, Theme theme) {
        DomainPreconditions.requireNonNull(createAt, DomainErrorCode.INVALID_INPUT, "createdAt");
        DomainPreconditions.requireNonBlank(name, DomainErrorCode.INVALID_INPUT, "name");
        DomainPreconditions.requireNonNull(date, DomainErrorCode.INVALID_INPUT, "date");
        DomainPreconditions.requireNonNull(time, DomainErrorCode.INVALID_INPUT, "time");
        DomainPreconditions.requireNonNull(theme, DomainErrorCode.INVALID_INPUT, "theme");
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
