package roomescape.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationFixture {

    public static final Long ID = 1L;
    public static final String CUSTOMER_NAME = "수달";

    public static Reservation saved(
        final Long id,
        final String customerName,
        final LocalDate date,
        final ReservationTime time,
        final Theme theme
    ) {
        return Reservation.of(id, customerName, date, time, theme);
    }

    public static Reservation saved(
        final LocalDate date,
        final ReservationTime time,
        final Theme theme
    ) {
        return Reservation.of(ID, CUSTOMER_NAME, date, time, theme);
    }
}
