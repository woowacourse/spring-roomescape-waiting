package roomescape.fixture;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;

public class WaitingFixture {

    public static Waiting saved(Long id, String customerName, LocalDate date,
            LocalDateTime createdAt, ReservationTime time, Theme theme) {
        return Waiting.of(id, customerName, Date.valueOf(date), createdAt, time, theme);
    }
}
