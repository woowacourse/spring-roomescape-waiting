package roomescape.feature.reservation.domain;

import java.time.LocalDate;
import lombok.Getter;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.error.exception.GeneralException;

@Getter
public class Reservation {

    private final Long id;
    private final ReserverName name;
    private final Schedule schedule;
    private final Theme theme;
    private final ReservationStatus status;
    private final long version;

    private Reservation(Long id, ReserverName name, Schedule schedule, Theme theme, ReservationStatus status,
        long version) {
        this.id = id;
        this.name = name;
        this.schedule = schedule;
        this.theme = theme;
        this.status = status;
        this.version = version;
    }

    public static Reservation create(ReserverName name, LocalDate date, Time time, Theme theme, ReservationStatus status) {
        Schedule schedule = new Schedule(date, time);
        validateFuture(schedule);

        return new Reservation(null, name, schedule, theme, status, 0L);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status) {
        return new Reservation(id, name, new Schedule(date, time), theme, status, 0L);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status, long version) {
        return new Reservation(id, name, new Schedule(date, time), theme, status, version);
    }

    public Reservation update(ReserverName requestName, LocalDate newDate, Time newTime, Theme newTheme) {
        Schedule newSchedule = new Schedule(newDate, newTime);
        validateUpdatable(requestName, newSchedule);
        validateChanged(newSchedule, newTheme);

        return new Reservation(this.id, this.name, newSchedule, newTheme, this.status, this.version);
    }

    public Reservation delete() {
        if (this.status == ReservationStatus.DELETED) {
            throw new GeneralException(ReservationErrorType.ALREADY_DELETED);
        }

        return new Reservation(this.id, this.name, this.schedule, this.theme, ReservationStatus.DELETED, this.version);
    }

    public Reservation cancelActive(ReserverName requestName) {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }
        validateCancelable(requestName);

        return new Reservation(this.id, this.name, this.schedule, this.theme, ReservationStatus.CANCELED, this.version);
    }

    public Reservation cancelWaiting(ReserverName requestName) {
        if (this.status != ReservationStatus.WAITING) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }
        validateCancelable(requestName);

        return new Reservation(this.id, this.name, this.schedule, this.theme, ReservationStatus.CANCELED, this.version);
    }

    public Reservation confirmWaiting() {
        if (this.status != ReservationStatus.WAITING) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }

        return new Reservation(this.id, this.name, this.schedule, this.theme, ReservationStatus.ACTIVE, this.version);
    }

    private void validateCancelable(ReserverName requestName) {
        if (!this.name.equals(requestName)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN);
        }
        if (this.schedule.isPast()) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CANCEL);
        }
    }

    private static void validateFuture(Schedule schedule) {
        if (schedule.isPast()) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CREATE);
        }
    }

    private void validateUpdatable(ReserverName requestName, Schedule newSchedule) {
        if (!this.name.equals(requestName)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN);
        }
        if (this.status != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        validateFuture(this.schedule);
        validateFuture(newSchedule);
    }

    private void validateChanged(Schedule newSchedule, Theme newTheme) {
        if (isSameSchedule(newSchedule) && isSameTheme(newTheme)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_CHANGED);
        }
    }

    private boolean isSameSchedule(Schedule newSchedule) {
        return this.schedule.date().equals(newSchedule.date())
            && isSameId(this.schedule.time().getId(), newSchedule.time().getId());
    }

    private boolean isSameTheme(Theme newTheme) {
        return isSameId(this.theme.getId(), newTheme.getId());
    }

    private boolean isSameId(Long currentId, Long newId) {
        return currentId != null && currentId.equals(newId);
    }

    public Slot getSlot() {
        return new Slot(getTime().getId(), theme.getId(), getDate());
    }

    public LocalDate getDate() {
        return schedule.date();
    }

    public Time getTime() {
        return schedule.time();
    }
}
