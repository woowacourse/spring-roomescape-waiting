package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public class TestFixture {

    public static ReservationTime createTimeWithoutId(int hour, int min) {
        return ReservationTime.createWithoutId(LocalTime.of(hour, min));
    }

    public static Theme createThemeWithoutId(String name, String description, String thumbnail) {
        return Theme.createWithoutId(name, description, thumbnail);
    }

    public static Member createMemberWithoutId(String name, String email, String password) {
        return Member.createWithoutId(name, email, password, Role.USER);
    }

    public static ReservationTime createTime(int hour, int min) {
        return ReservationTime.createWithoutId(LocalTime.of(hour, min));
    }

    public static Theme createTheme(String name, String description, String thumbnail) {
        Theme theme = Theme.createWithoutId(name, description, thumbnail);
        ReflectionTestUtils.setField(theme, "id", 1L);
        return theme;
    }

    public static Member createMember(String name, String email, String password) {
        Member member = Member.createWithoutId(name, email, password, Role.USER);
        ReflectionTestUtils.setField(member, "id", 1L);
        return member;
    }

    public static Waiting createWaiting(Member member, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        Waiting waiting = Waiting.createWithoutId(member, date, time, theme, WaitingStatus.PENDING, createdAt);
        ReflectionTestUtils.setField(waiting, "id", 1L);
        return waiting;
    }

}
