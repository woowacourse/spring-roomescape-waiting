package roomescape.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationSpecFixture {
    public static ReservationSpec createSpec(LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSpec(new ReservationDate(date), time, theme);
    }
}
