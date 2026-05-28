package roomescape.reservation.domain;

import java.time.LocalDate;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public record Reservation(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
    public static Reservation of(String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, name, date, time, theme);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.name, this.date, this.time, this.theme);
    }

    public Reservation updateDate(LocalDate date) {
        return new Reservation(this.id, this.name, date, this.time, this.theme);
    }

    public Reservation updateTime(ReservationTime time) {
        return new Reservation(this.id, this.name, this.date, time, this.theme);
    }

    public boolean hasSameName(String name) {
        return this.name.equals(name);
    }
}
