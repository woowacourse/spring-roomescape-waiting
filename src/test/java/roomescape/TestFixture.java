package roomescape;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import roomescape.domain.BookingSlot;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public class TestFixture {
    public static final LocalDate DEFAULT_DATE = LocalDate.now().plusDays(1);

    public static Reservation createDefaultReservation_1() {
        return createNewReservation(createMemberByName("member1"), DEFAULT_DATE, createDefaultReservationTime(), createDefaultTheme());
    }

    public static Reservation createDefaultReservation_2() {
        return createNewReservation(createMemberByName("member2"), DEFAULT_DATE.plusDays(1), createDefaultReservationTime(), createDefaultTheme());
    }

    public static Reservation createReservationByMember(Member member) {
        return Reservation.create(member, new BookingSlot(DEFAULT_DATE, createDefaultReservationTime(), createDefaultTheme()));
    }

    public static Reservation createNewReservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return Reservation.create(member, new BookingSlot(date, time, theme));
    }

    public static Waiting createDefaultWaiting_1() {
        return createWaiting(createMemberByName("member1"), DEFAULT_DATE, createDefaultReservationTime(), createDefaultTheme());
    }

    public static Waiting createDefaultWaiting_2() {
        return createWaiting(createMemberByName("member2"), DEFAULT_DATE, createDefaultReservationTime(), createDefaultTheme());
    }


    public static Waiting createWaiting(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return Waiting.create(member, new BookingSlot(date, time, theme));
    }

    public static Waiting createWaitingByMember(Member member) {
        return Waiting.create(member, new BookingSlot(DEFAULT_DATE, createDefaultReservationTime(), createDefaultTheme()));
    }

    public static ReservationTime createDefaultReservationTime() {
        return ReservationTime.createNew(LocalTime.of(12, 0));
    }

    public static ReservationTime createTimeFrom(LocalTime time) {
        return ReservationTime.createNew(time);
    }

    public static Member createDefaultMember() {
        return Member.createNew("name", MemberRole.USER, "email", "password");
    }

    public static Member createAdminMember() {
        return Member.createNew("admin", MemberRole.ADMIN, "admin@email.com", "password");
    }

    public static Member createMemberByName(String name) {
        return Member.createNew(name, MemberRole.USER, "email" + name, "password");
    }

    public static Theme createDefaultTheme() {
        return new Theme("themeName", "description", "thumbnail");
    }

    public static Theme createThemeByName(String name) {
        return new Theme(name, "description", "thumbnail");
    }

    public static Clock fixedClockAt(LocalDateTime fixedDateTime) {
        return Clock.fixed(fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }
}
