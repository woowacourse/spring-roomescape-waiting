package roomescape.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationDate;

public class ReservationDateFixture {

    public static ReservationDate 예약날짜_내일 = new ReservationDate(LocalDate.now().plusDays(1));
    public static ReservationDate 예약날짜_오늘 = new ReservationDate(LocalDate.now());
    public static ReservationDate 예약날짜_7일전 = new ReservationDate(LocalDate.now().minusDays(7));
}
