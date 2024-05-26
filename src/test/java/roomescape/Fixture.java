package roomescape;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.service.theme.dto.ThemeRequest;

public class Fixture {
    public static final Theme theme = new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

    public static final ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

    public static final LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);

    public static final LocalDate today = LocalDate.now();
    public static final LocalDate tomorrow = today.plusDays(1);

    public static final ReservationTime reservationTime = new ReservationTime(currentTime);

    public static final Member member = new Member("member", "member@email.com", "member123", Role.MEMBER);
    public static final Member member2 = new Member("pedro", "pedro@email.com", "pedro123", Role.MEMBER);
    public static final Member member3 = new Member("lini", "lini@email.com", "lini123", Role.MEMBER);

}
