package roomescape.fixture;

import roomescape.domain.ReservationDate;

import java.time.LocalDate;

public class ReservationDateFixture {

    private ReservationDateFixture() {}

    public static final ReservationDate 예약날짜_25_4_23 = new ReservationDate(LocalDate.of(2025, 4, 23));
    public static final ReservationDate 예약날짜_25_4_22 = new ReservationDate(LocalDate.of(2025, 4, 22));
    public static final ReservationDate 예약날짜_오늘 = new ReservationDate(LocalDate.of(2025, 4, 20));
    public static final ReservationDate 예약날짜_7일전 = new ReservationDate(LocalDate.of(2025, 4, 13));
}
