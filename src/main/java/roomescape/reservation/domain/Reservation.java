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
import static roomescape.reservation.exception.ReservationErrorCode.*;

@Getter
public class Reservation {
    private final Long id;
    private final String guestName;
    private final ReservationSlot reservationSlot;
    private final Status status;
    private final LocalDateTime lastModifiedAt;

    private Reservation(
            Long id, String guestName, ReservationSlot reservationSlot, Status status, LocalDateTime lastModifiedAt) {
        validateReservation(guestName, reservationSlot, status, lastModifiedAt);
        this.id = id;
        this.guestName = guestName;
        this.reservationSlot = reservationSlot;
        this.status = status;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static Reservation create(
            String guestName, ReservationSlot reservationSlot, Status status, LocalDateTime lastModifiedAt) {
        return new Reservation(null, guestName, reservationSlot, status, lastModifiedAt);
    }

    public static Reservation of(
            long id,
            String guestName,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            Status status,
            LocalDateTime lastModifiedAt
    ) {
        return of(id, guestName, ReservationSlot.create(date, time, theme), status, lastModifiedAt);
    }

    public static Reservation of(
            long id,
            String guestName,
            ReservationSlot reservationSlot,
            Status status,
            LocalDateTime lastModifiedAt
    ) {
        return new Reservation(id, guestName, reservationSlot, status, lastModifiedAt);
    }

    public Reservation withId(long id) {
        require(this.id == null, new DomainException(RESERVATION_ALREADY_HAS_ID));
        return of(id, guestName, reservationSlot, status, lastModifiedAt);
    }

    private void validateReservation(
            String guestName,
            ReservationSlot reservationSlot,
            Status status,
            LocalDateTime lastModifiedAt
    ) {
        requireNonBlank(guestName, new DomainException(INVALID_RESERVATION_GUEST_NAME));
        requireNonNull(reservationSlot, new DomainException(INVALID_RESERVATION_SLOT));
        requireNonNull(status, new DomainException(INVALID_STATUS));
        requireNonNull(lastModifiedAt, new DomainException(INVALID_LAST_MODIFIED_AT));
    }

    public LocalDate getDate() {
        return reservationSlot.getDate();
    }

    public ReservationTime getTime() {
        return reservationSlot.getTime();
    }

    public Theme getTheme() {
        return reservationSlot.getTheme();
    }

    public Long getThemeId() {
        return reservationSlot.getThemeId();
    }

    public Long getTimeId() {
        return reservationSlot.getTimeId();
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
        return reservationSlot.isPassed(now);
    }

    public boolean isSameGuest(String guestName) {
        return Objects.equals(this.guestName, guestName);
    }

    public Reservation changeDateTimeAndStatus(
            LocalDate changedDate, ReservationTime changedTime, Status status, LocalDateTime lastModifiedAt) {
        ReservationSlot changedSlot = reservationSlot.changeDateTime(changedDate, changedTime);
        return new Reservation(id, guestName, changedSlot, status, lastModifiedAt);
    }

    public Reservation changeStatus(Status status) {
        return new Reservation(id, guestName, reservationSlot, status, lastModifiedAt);
    }

    public boolean isConfirmed() {
        return CONFIRMED.equals(status);
    }

    public boolean isCanceled() {
        return CANCELED.equals(status);
    }

    public boolean isSameDateTime(LocalDate date, Long timeId) {
        return reservationSlot.isSameDateTime(date, timeId);
    }
}
