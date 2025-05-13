package roomescape;

import java.time.LocalTime;
import roomescape.application.member.MemberResult;
import roomescape.application.reservation.dto.ReservationTimeResult;
import roomescape.application.reservation.dto.ThemeResult;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public final class TestFixtures {
    public static final Member NORMAL_MEMBER_1 = new Member(1L, "test1", new Email("email1@gmail.com"), "password",
            Role.NORMAL);
    public static final Member NORMAL_MEMBER_2 = new Member(2L, "test2", new Email("email2@gmail.com"), "password",
            Role.NORMAL);
    public static final MemberResult NORMAL_MEMBER_1_RESULT = new MemberResult(NORMAL_MEMBER_1.getId(),
            NORMAL_MEMBER_1.getName());
    public static final MemberResult NORMAL_MEMBER_2_RESULT = new MemberResult(NORMAL_MEMBER_2.getId(),
            NORMAL_MEMBER_2.getName());


    public static final Theme THEME_1 = new Theme(1L, "test", "description", "thumbnail");
    public static final Theme THEME_2 = new Theme(2L, "test2", "description2", "thumbnail2");
    public static final Theme THEME_3 = new Theme(3L, "test3", "description3", "thumbnail3");
    public static final Theme THEME_4 = new Theme(4L, "test4", "description4", "thumbnail4");
    public static final ThemeResult THEME_1_RESULT = new ThemeResult(
            THEME_1.getId(),
            THEME_1.getName(),
            THEME_1.getDescription(),
            THEME_1.getThumbnail());
    public static final ThemeResult THEME_2_RESULT = new ThemeResult(
            THEME_2.getId(),
            THEME_2.getName(),
            THEME_2.getDescription(),
            THEME_2.getThumbnail());
    public static final ThemeResult THEME_3_RESULT = new ThemeResult(
            THEME_3.getId(),
            THEME_3.getName(),
            THEME_3.getDescription(),
            THEME_3.getThumbnail());
    public static final ThemeResult THEME_4_RESULT = new ThemeResult(
            THEME_4.getId(),
            THEME_4.getName(),
            THEME_4.getDescription(),
            THEME_4.getThumbnail());

    public static final ReservationTime RESERVATION_TIME_1 = new ReservationTime(1L, LocalTime.of(12, 0));
    public static final ReservationTime RESERVATION_TIME_2 = new ReservationTime(2L, LocalTime.of(13, 0));
    public static final ReservationTime RESERVATION_TIME_3 = new ReservationTime(3L, LocalTime.of(14, 0));
    public static final ReservationTime RESERVATION_TIME_4 = new ReservationTime(4L, LocalTime.of(15, 0));
    public static final ReservationTime RESERVATION_TIME_5 = new ReservationTime(5L, LocalTime.of(16, 0));
    public static final ReservationTimeResult RESERVATION_TIME_1_RESULT = new ReservationTimeResult(
            RESERVATION_TIME_1.id(), RESERVATION_TIME_1.startAt());
    public static final ReservationTimeResult RESERVATION_TIME_2_RESULT = new ReservationTimeResult(
            RESERVATION_TIME_2.id(), RESERVATION_TIME_2.startAt());
    public static final ReservationTimeResult RESERVATION_TIME_3_RESULT = new ReservationTimeResult(
            RESERVATION_TIME_3.id(), RESERVATION_TIME_3.startAt());
    public static final ReservationTimeResult RESERVATION_TIME_4_RESULT = new ReservationTimeResult(
            RESERVATION_TIME_4.id(), RESERVATION_TIME_4.startAt());
    public static final ReservationTimeResult RESERVATION_TIME_5_RESULT = new ReservationTimeResult(
            RESERVATION_TIME_5.id(), RESERVATION_TIME_5.startAt());
}
