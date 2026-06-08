package roomescape.domain.reservation.entity;

import java.time.LocalDate;
import roomescape.domain.reservation.vo.ReservationSchedule;
import roomescape.domain.reservation.vo.ReserverName;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

public class Reservation {

    private final Long id;
    private final ReserverName name;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final ReservationStatus status;
    private final Long version;

    private Reservation(Long id, ReserverName name, LocalDate date, Time time, Theme theme, ReservationStatus status,
        Long version) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.version = version;
    }

    public static Reservation create(ReserverName name, LocalDate date, Time time, Theme theme) {
        return new Reservation(null, name, date, time, theme, ReservationStatus.ACTIVE, 0L);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date, Time time, Theme theme, ReservationStatus status, Long version) {
        return new Reservation(id, name, date, time, theme, status, version);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date, Time time, Theme theme, ReservationStatus status) {
        return reconstruct(id, name, date, time, theme, status, 0L);
    }

    public Reservation cancel() {
        return new Reservation(this.id, this.name, this.date, this.time, this.theme, ReservationStatus.CANCELED,
            version);
    }

    public Reservation toWaiting() {
        return new Reservation(this.id, this.name, this.date, this.time, this.theme, ReservationStatus.WAITING,
            version);
    }

    public Reservation toActive() {
        return new Reservation(this.id, this.name, this.date, this.time, this.theme, ReservationStatus.ACTIVE, version);
    }

    public ReservationEditableStatus getEditableStatus(LocalDate now) {
        if (status == ReservationStatus.CANCELED) {
            return ReservationEditableStatus.CANCELED;
        }

        if (isPast(now)) {
            return ReservationEditableStatus.LOCKED;
        }

        if (status == ReservationStatus.WAITING) {
            return ReservationEditableStatus.WAITING;
        }

        if (time.isDeleted() || theme.isDeleted()) {
            return ReservationEditableStatus.EDIT_RECOMMENDED;
        }

        return ReservationEditableStatus.EDITABLE;
    }

    public ReservationSchedule getSchedule() {
        return new ReservationSchedule(date, theme.getId(), time.getId());
    }

    public boolean isReservedBy(ReserverName name) {
        return this.name.equals(name);
    }

    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    public boolean isScheduleChanged(Reservation reservation) {
        return !getSchedule().equals(reservation.getSchedule());
    }

    public boolean isPast(LocalDate date) {
        return this.date.isBefore(date);
    }

    public Long getId() {
        return id;
    }

    public ReserverName getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }
}
