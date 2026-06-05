package roomescape.domain.reservation.entity;

import java.time.LocalDate;
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

    private Reservation(Long id, ReserverName name, LocalDate date, Time time, Theme theme, ReservationStatus status) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation create(ReserverName name, LocalDate date, Time time, Theme theme) {
        return new Reservation(null, name, date, time, theme, ReservationStatus.ACTIVE);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date, Time time, Theme theme, ReservationStatus status) {
        return new Reservation(id, name, date, time, theme, status);
    }

    public Reservation cancel() {
        return new Reservation(this.id, this.name, this.date, this.time, this.theme, ReservationStatus.CANCELED);
    }

    public Reservation toWaiting() {
        return new Reservation(this.id, this.name, this.date, this.time, this.theme, ReservationStatus.WAITING);
    }

    public Reservation toActive() {
        return new Reservation(this.id, this.name, this.date, this.time, this.theme, ReservationStatus.ACTIVE);
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
}
