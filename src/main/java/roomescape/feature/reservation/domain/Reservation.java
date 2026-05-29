package roomescape.feature.reservation.domain;

import java.time.LocalDate;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.error.exception.GeneralException;

public class Reservation {

    private final Long id;
    private final ReserverName name;
    private final Schedule schedule;
    private final Theme theme;
    private final ReservationStatus status;

    private Reservation(Long id, ReserverName name, Schedule schedule, Theme theme, ReservationStatus status) {
        this.id = id;
        this.name = name;
        this.schedule = schedule;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation create(ReserverName name, LocalDate date, Time time, Theme theme) {
        Schedule schedule = new Schedule(date, time);
        validateFuture(schedule);

        return new Reservation(null, name, schedule, theme, ReservationStatus.ACTIVE);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status) {
        return new Reservation(id, name, new Schedule(date, time), theme, status);
    }

    public Reservation cancel() {
        return new Reservation(this.id, this.name, this.schedule, this.theme, ReservationStatus.CANCELED);
    }

    public Reservation toWaiting() {
        return new Reservation(this.id, this.name, this.schedule, this.theme, ReservationStatus.WAITING);
    }

    private static void validateFuture(Schedule schedule) {
        if (schedule.isPast()) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CREATE);
        }
    }

    public Long getId() {
        return id;
    }

    public ReserverName getName() {
        return name;
    }

    public LocalDate getDate() {
        return schedule.date();
    }

    public Time getTime() {
        return schedule.time();
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
