package roomescape.fixture;

import java.time.LocalDate;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;

public enum ReservationFixture {
    FUTURE("예약자", LocalDate.now().plusYears(1)),
    PAST("예약자", LocalDate.now().minusYears(1));

    private final String name;
    private final LocalDate date;

    ReservationFixture(String name, LocalDate date) {
        this.name = name;
        this.date = date;
    }

    public Reservation createInstance(Time time, Theme theme) {
        return Reservation.create(new ReserverName(name), date, time, theme, ReservationStatus.ACTIVE);
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }
}
