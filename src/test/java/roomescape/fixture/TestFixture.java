package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class TestFixture {

    public static final LocalDate FUTURE_DATE = TestFixture.makeAfterOneWeekDate();
    public static final LocalDateTime NOW_DATETIME = TestFixture.makeNowDateTime();
    public static final LocalDate NOW_DATE = TestFixture.makeNowDate();

    public static Theme makeTheme() {
        return new Theme("추리", "셜록 추리 게임 with Danny", "image.png");
    }

    public static LocalDateTime makeTimeAfterOneHour() {
        return LocalDateTime.now().plusHours(1);
    }

    public static ReservationSlot makeConfirmedReservation(final LocalDate date, final ReservationTime reservationTime,
                                                           final Member member, final Theme theme) {
        ReservationSlot reservationSlot = new ReservationSlot(date, reservationTime, theme);
        reservationSlot.addReservation(member, NOW_DATETIME);
        return reservationSlot;
    }

    public static ReservationTime makeReservationTime(final LocalTime localTime) {
        return new ReservationTime(localTime);
    }

    public static Member makeMember() {
        return new Member("Mint", "mint@gmail.com", "password", MemberRole.REGULAR);
    }

    private static LocalDate makeAfterOneWeekDate() {
        return LocalDate.now().plusDays(7);
    }

    private static LocalDateTime makeNowDateTime() {
        return LocalDateTime.now();
    }

    private static LocalDate makeNowDate() {
        return LocalDate.now();
    }
}
