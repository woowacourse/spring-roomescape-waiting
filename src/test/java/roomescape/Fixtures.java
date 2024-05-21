package roomescape;

import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Fixtures {

    public static final Member memberFixture = new Member(
            1L,
            "클로버",
            "clover@gmail.com",
            "password"
    );

    public static final List<Member> memberFixtures = List.of(
            new Member(2L, "클로버2", "test2@gmail.com", "password"),
            new Member(3L, "클로버3", "test3@gmail.com", "password"),
            new Member(3L, "클로버3", "test3@gmail.com", "password"),
            new Member(4L, "클로버4", "test4@gmail.com", "password"),
            new Member(5L, "클로버5", "test5@gmail.com", "password"),
            new Member(6L, "클로버6", "test6@gmail.com", "password"),
            new Member(7L, "클로버7", "test7@gmail.com", "password"),
            new Member(8L, "클로버8", "test8@gmail.com", "password"),
            new Member(9L, "클로버9", "test9@gmail.com", "password"),
            new Member(10L, "클로버10", "test10@gmail.com", "password")
    );

    public static final LoginMember loginMemberFixture = new LoginMember(
            memberFixture.getId(),
            memberFixture.getEmail()
    );

    public static final ReservationTime reservationTimeFixture = new ReservationTime(
            1L,
            LocalTime.of(10, 10)
    );

    public static final Theme themeFixture = new Theme(
            1L,
            "공포",
            "완전 무서운 테마",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );

    public static final Reservation reservationFixture = new Reservation(
            1L,
            memberFixture,
            LocalDate.now().plusMonths(6),
            reservationTimeFixture,
            themeFixture
    );

    public static final List<Theme> themeFixtures = List.of(
            new Theme(1L, "테마 이름 1", "테마 설명 1", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(2L, "테마 이름 2", "테마 설명 2", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(3L, "테마 이름 3", "테마 설명 3", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(4L, "테마 이름 4", "테마 설명 4", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(5L, "테마 이름 5", "테마 설명 5", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(6L, "테마 이름 6", "테마 설명 6", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(7L, "테마 이름 7", "테마 설명 7", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(8L, "테마 이름 8", "테마 설명 8", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(9L, "테마 이름 9", "테마 설명 9", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(10L, "테마 이름 10", "테마 설명 10", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            new Theme(11L, "테마 이름 11", "테마 설명 11", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg1")
    );

    public static final List<ReservationTime> timeFixtures = List.of(
            new ReservationTime(1L, LocalTime.of(10, 10)),
            new ReservationTime(2L, LocalTime.of(10, 15)),
            new ReservationTime(3L, LocalTime.of(10, 20)),
            new ReservationTime(4L, LocalTime.of(10, 25)),
            new ReservationTime(5L, LocalTime.of(10, 30))
    );

    public static final List<Reservation> reservationFixturesForPopularTheme = List.of(
            new Reservation(1L, memberFixtures.get(0), LocalDate.now().minusDays(3), timeFixtures.get(0), themeFixtures.get(0)),
            new Reservation(2L, memberFixtures.get(1), LocalDate.now().minusDays(3), timeFixtures.get(1), themeFixtures.get(0)),
            new Reservation(3L, memberFixtures.get(2), LocalDate.now().minusDays(3), timeFixtures.get(2), themeFixtures.get(0)),
            new Reservation(4L, memberFixtures.get(3), LocalDate.now().minusDays(3), timeFixtures.get(3), themeFixtures.get(0)),
            new Reservation(5L, memberFixtures.get(4), LocalDate.now().minusDays(3), timeFixtures.get(4), themeFixtures.get(0)),
            new Reservation(6L, memberFixtures.get(5), LocalDate.now().minusDays(3), timeFixtures.get(0), themeFixtures.get(1)),
            new Reservation(7L, memberFixtures.get(6), LocalDate.now().minusDays(3), timeFixtures.get(1), themeFixtures.get(1)),
            new Reservation(8L, memberFixtures.get(7), LocalDate.now().minusDays(3), timeFixtures.get(2), themeFixtures.get(1)),
            new Reservation(9L, memberFixtures.get(8), LocalDate.now().minusDays(3), timeFixtures.get(3), themeFixtures.get(1)),
            new Reservation(10L, memberFixtures.get(9), LocalDate.now().minusDays(3), timeFixtures.get(0), themeFixtures.get(2)),
            new Reservation(11L, memberFixtures.get(0), LocalDate.now().minusDays(4), timeFixtures.get(1), themeFixtures.get(2)),
            new Reservation(12L, memberFixtures.get(1), LocalDate.now().minusDays(4), timeFixtures.get(2), themeFixtures.get(2)),
            new Reservation(13L, memberFixtures.get(2), LocalDate.now().minusDays(4), timeFixtures.get(0), themeFixtures.get(3)),
            new Reservation(14L, memberFixtures.get(3), LocalDate.now().minusDays(4), timeFixtures.get(0), themeFixtures.get(4)),
            new Reservation(15L, memberFixtures.get(4), LocalDate.now().minusDays(4), timeFixtures.get(1), themeFixtures.get(4)),
            new Reservation(16L, memberFixtures.get(5), LocalDate.now().minusDays(4), timeFixtures.get(2), themeFixtures.get(4)),
            new Reservation(17L, memberFixtures.get(6), LocalDate.now().minusDays(4), timeFixtures.get(3), themeFixtures.get(4)),
            new Reservation(18L, memberFixtures.get(7), LocalDate.now().minusDays(4), timeFixtures.get(4), themeFixtures.get(4)),
            new Reservation(19L, memberFixtures.get(8), LocalDate.now().minusDays(4), timeFixtures.get(0), themeFixtures.get(4)),
            new Reservation(20L, memberFixtures.get(9), LocalDate.now().minusDays(4), timeFixtures.get(1), themeFixtures.get(4))
    );
}
