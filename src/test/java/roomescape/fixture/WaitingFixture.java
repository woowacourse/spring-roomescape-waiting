package roomescape.fixture;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;

public class WaitingFixture {

    public static final Long ID = 1L;
    public static final String CUSTOMER_NAME = "수달";

    public static Waiting saved(
        final Long id,
        final String customerName,
        final LocalDate date,
        final LocalDateTime createdAt,
        final ReservationTime time,
        final Theme theme
    ) {
        return Waiting.of(id, customerName, Date.valueOf(date), createdAt, time, theme);
    }

    public static Waiting saved(
        final LocalDate reservationDate,
        final LocalDateTime createdAt,
        final ReservationTime time,
        final Theme theme
    ) {
        return Waiting.of(ID, CUSTOMER_NAME, Date.valueOf(reservationDate), createdAt, time, theme);
    }
}
