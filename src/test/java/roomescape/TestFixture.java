package roomescape;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;
import roomescape.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class TestFixture {
    public static final LocalDate DEFAULT_DATE = LocalDate.of(2025, 1, 1);
    public static final String AUTH_COOKIE_NAME = "token";

    public static ReservationTime createDefaultReservationTime() {
        return ReservationTime.createNew(LocalTime.of(12, 0));
    }

    public static ReservationTime createDefaultReservationTimeByTime(LocalTime time) {
        return ReservationTime.createNew(time);
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

    public static Cookie createAuthCookie(String token) {
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }

    public static ResponseCookie createAuthResponseCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE_NAME)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build();
    }
}
