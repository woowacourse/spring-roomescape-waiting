package roomescape;

import roomescape.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class TestFixture {
    public static final LocalDate DEFAULT_DATE = LocalDate.of(2025, 1, 1);

    public static ReservationTime createDefaultReservationTime() {
        return ReservationTime.createNew(LocalTime.of(12, 0));
    }

    public static Member createDefaultMember() {
        return Member.createNew("name", MemberRole.USER, "email", "password");
    }

    public static Member createMemberByName(String name) {
        return Member.createNew(name, MemberRole.USER, "email", "password");
    }

    public static Theme createDefaultTheme() {
        return new Theme("theme", "description", "thumbnail");
    }

    public static Theme createThemeByName(String name) {
        return new Theme(name, "description", "thumbnail");
    }

    public static Reservation createDefaultReservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return Reservation.createNew(member, date, time, theme);
    }
}
