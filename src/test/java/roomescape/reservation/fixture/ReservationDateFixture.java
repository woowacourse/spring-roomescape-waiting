package roomescape.reservation.fixture;

import static roomescape.common.Constant.NOW;
import static roomescape.common.Constant.TOMORROW;
import static roomescape.common.Constant.YESTERDAY;

import roomescape.reservation.domain.ReservationDate;

public class ReservationDateFixture {

    public static ReservationDate 예약날짜_내일 = new ReservationDate(TOMORROW.toLocalDate());

    public static ReservationDate 예약날짜_오늘 = new ReservationDate(NOW.toLocalDate());
    public static ReservationDate 예약날짜_어제 = new ReservationDate(YESTERDAY.toLocalDate());
    public static ReservationDate 예약날짜_3일전 = new ReservationDate(NOW.toLocalDate().minusDays(3));
    public static ReservationDate 예약날짜_7일전 = new ReservationDate(NOW.toLocalDate().minusDays(7));
    public static ReservationDate 예약날짜_8일전 = new ReservationDate(NOW.toLocalDate().minusDays(8));
}
