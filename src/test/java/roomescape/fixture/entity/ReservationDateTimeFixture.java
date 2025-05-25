package roomescape.fixture.entity;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.time.domain.ReservationTime;

public class ReservationDateTimeFixture {

    public static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    public static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);

    public static ReservationDateTime create() {
        return ReservationDateTime.create(
                new ReservationDate(TOMORROW),
                ReservationTimeFixture.create()
        );
    }

    public static ReservationDateTime create(LocalDate date, ReservationTime time) {
        return ReservationDateTime.create(
                new ReservationDate(date),
                time
        );
    }

    public static ReservationDateTime createTomorrow(ReservationTime time) {
        return create(TOMORROW, time);
    }

    public static ReservationDateTime createYesterday(ReservationTime time) {
        return create(YESTERDAY, time);
    }
} 
