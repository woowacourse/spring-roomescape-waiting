package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime reservationTime;
    private final Theme theme;

    public Reservation(Long id, String name, LocalDate date, ReservationTime reservationTime, Theme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    public static Reservation of(String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, name, date, time, theme);
    }

    public Reservation updateId(long id) {
        return new Reservation(
                id,
                this.name,
                this.date,
                this.reservationTime,
                this.theme
        );
    }

    public Reservation updateDate(LocalDate date) {
        return new Reservation(
                this.id,
                this.name,
                date,
                this.reservationTime,
                this.theme
        );
    }

    public Reservation updateTime(ReservationTime time) {
        return new Reservation(
                this.id,
                this.name,
                this.date,
                time,
                this.theme
        );
    }

    public boolean hasSameName(String name) {
        return this.name.equals(name);
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

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Reservation that = (Reservation) o;
        return id != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
