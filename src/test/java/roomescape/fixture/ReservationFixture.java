package roomescape.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationFixture {

    public static final Long ID = 1L;
    public static final String CUSTOMER_NAME = "브라운";

    public static Reservation saved(Long id, String customerName, LocalDate date,
            ReservationTime time, Theme theme) {
        return Reservation.of(id, customerName, date, time, theme);
    }

    public static Reservation saved(LocalDate date, ReservationTime time, Theme theme) {
        return Reservation.of(ID, CUSTOMER_NAME, date, time, theme);
    }
}
