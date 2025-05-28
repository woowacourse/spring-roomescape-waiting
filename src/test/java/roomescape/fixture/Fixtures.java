package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public class Fixtures {
    // Member fixture
    public static Member member() {
        return new Member(null, "히스타", MemberRole.USER, "hista@email.com", "password");
    }

    // Theme fixtures
    public static Theme theme() {
        return new Theme(null, "테마 이름", "테마 설명", "테마 썸네일");
    }

    public static Theme theme1() {
        return new Theme(null, "테마 이름1", "테마 설명1", "테마 썸네일1");
    }

    public static Theme theme2() {
        return new Theme(null, "테마 이름2", "테마 설명2", "테마 썸네일2");
    }

    // ReservationTime fixtures
    public static ReservationTime reservationTime() {
        return new ReservationTime(null, LocalTime.of(12, 0));
    }

    public static ReservationTime reservationTime1() {
        return new ReservationTime(null, LocalTime.of(12, 0));
    }

    public static ReservationTime reservationTime2() {
        return new ReservationTime(null, LocalTime.of(13, 0));
    }

    // Date fixtures
    public static LocalDate oneDayPlusDate() {
        return LocalDate.now().plusDays(1);
    }

    public static LocalDate oneDayMinusDate() {
        return LocalDate.now().minusDays(1);
    }

    public static LocalDate twoDayPlusDate() {
        return LocalDate.now().plusDays(2);
    }

    // Time fixture
    public static LocalTime oneHourPlusTime() {
        return LocalTime.now().plusHours(1);
    }
}
