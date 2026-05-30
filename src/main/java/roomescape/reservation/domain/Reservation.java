package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.common.exception.DomainException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static roomescape.common.domain.DomainPreconditions.require;
import static roomescape.common.domain.DomainPreconditions.requireNonBlank;
import static roomescape.common.domain.DomainPreconditions.requireNonNull;
import static roomescape.reservation.domain.Status.*;
import static roomescape.reservation.domain.Status.CANCELED;
import static roomescape.reservation.exception.ReservationErrorCode.*;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;
import static roomescape.theme.exception.ThemeErrorCode.*;

@Getter
public class Reservation {
    private final Long id;
    private final String guestName;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Status status;
    private final LocalDateTime lastModifiedAt;

    private Reservation(
            Long id, String guestName, LocalDate date, ReservationTime time, Theme theme, Status status, LocalDateTime lastModifiedAt) {
        validateReservation(guestName, date, time, theme, lastModifiedAt);
        this.id = id;
        this.guestName = guestName;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static Reservation create(
            String guestName, LocalDate date, ReservationTime time, Theme theme, Status status, LocalDateTime changedAt) {
        return new Reservation(null, guestName, date, time, theme, status, changedAt);
    }

    public static Reservation of(
            long id,
            String guestName,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            Status status,
            LocalDateTime changedAt
    ) {
        return new Reservation(id, guestName, date, time, theme, status, changedAt);
    }

    public static Reservation clone(Reservation reservation) {
        return new Reservation(
                reservation.id,
                reservation.guestName,
                reservation.date,
                reservation.time,
                reservation.theme,
                reservation.status,
                reservation.lastModifiedAt);
    }

    public Reservation withId(long id) {
        require(this.id == null, new DomainException(RESERVATION_ALREADY_HAS_ID));
        return of(id, guestName, date, time, theme, status, lastModifiedAt);
    }

    private void validateReservation(String guestName, LocalDate date, ReservationTime time, Theme theme, LocalDateTime changedAt) {
        requireNonBlank(guestName, new DomainException(INVALID_RESERVATION_GUEST_NAME));
        requireNonNull(date, new DomainException(INVALID_RESERVATION_DATE));
        requireNonNull(time, new DomainException(INVALID_RESERVATION_TIME));
        requireNonNull(theme, new DomainException(INVALID_THEME));
        requireNonNull(changedAt, new DomainException(INVALID_LAST_MODIFIED_AT));
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Long getTimeId() {
        return time.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public boolean isPassed(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt())
                .isBefore(now);
    }

    public boolean isSameGuest(String guestName) {
        return Objects.equals(this.guestName, guestName);
    }

    public Reservation changeDateTimeAndStatus(
            LocalDate changedDate, ReservationTime changedTime, Status status, LocalDateTime lastModifiedAt) {
        return new Reservation(id, guestName, changedDate, changedTime, theme, status, lastModifiedAt);
    }

    public Reservation changeStatus(Status status) {
        return new Reservation(id, guestName, date, time, theme, status, lastModifiedAt);
    }

    public boolean isConfirmed() {
        return CONFIRMED.equals(status);
    }

    public boolean isCanceled() {
        return CANCELED.equals(status);
    }

    public boolean isSameDateTime(LocalDate date, Long timeId) {
        return this.date.isEqual(date) && Objects.equals(this.time.getId(), timeId);
    }
}
