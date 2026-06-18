package roomescape.reservation.fixture;

import roomescape.date.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class ReservationSlotFixture {

    public static ReservationSlot reservationSlot(
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return ReservationSlot.create(date, time, theme);
    }

}
