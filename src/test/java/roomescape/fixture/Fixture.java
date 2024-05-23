package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.Theme;

public class Fixture {

    private Fixture() {
    }

    public static final Theme THEME_1 = new Theme("테마1", "테마1 설명", "https://example1.com");

    public static final ReservationTime RESERVATION_TIME_1 = new ReservationTime(LocalTime.of(10, 0));
    public static final ReservationTime RESERVATION_TIME_2 = new ReservationTime(LocalTime.of(12, 0));

    public static final LocalDate DATE_1 = LocalDate.of(2024, 4, 9);

    public static final ReservationDetail RESERVATION_DETAIL_1 = new ReservationDetail(
            DATE_1,
            RESERVATION_TIME_1,
            THEME_1
    );

    public static final Member MEMBER_1 = new Member("user1@gmail.com", "password", "user1", Role.USER);
    public static final Member MEMBER_2 = new Member("user2@gmail.com", "password", "user2", Role.USER);
}
