package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

public class Fixture {

    public static final Member MEMBER1 = new Member(
            1L, new MemberName("제리"), new Email("jerry@gmail.com"), new Password("password"), Role.ADMIN
    );

    public static final LocalDate DATE = LocalDate.parse("2024-12-31");

    public static final ReservationTime RESERVATION_TIME1 = new ReservationTime(
            1L, LocalTime.parse("10:00")
    );

    public static final Theme THEME1 = new Theme(
            1L, new ThemeName("링"), "이거 겁나 무서움", "링 썸네일"
    );
}
