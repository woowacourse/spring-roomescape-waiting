package roomescape.fixture;

import java.time.LocalDate;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

public enum ReservationFixture {
    FUTURE("예약자", LocalDate.of(2099, 5, 1)),
    PAST("예약자", LocalDate.of(2000, 1, 1));

    private final String name;
    private final LocalDate date;

    ReservationFixture(String name, LocalDate date) {
        this.name = name;
        this.date = date;
    }

    public Reservation createInstance(Time time, Theme theme) {
        return Reservation.create(name, date, time, theme);
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }
}
