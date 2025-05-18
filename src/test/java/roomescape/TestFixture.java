package roomescape;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;
import roomescape.domain.*;
import roomescape.service.result.MemberResult;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.service.result.ThemeResult;

import java.time.LocalDate;
import java.time.LocalTime;

public class TestFixture {
    public static final LocalDate TEST_DATE = LocalDate.of(2025, 1, 1);
    public static final String AUTH_COOKIE_NAME = "token";

    // 추가된 공통 테스트 상수
    public static final String VALID_TOKEN = "header.payload.signature";
    public static final String TEST_EMAIL = "test@email.com";
    public static final String TEST_PASSWORD = "password";
    public static final String TEST_NAME = "test";
    public static final Long TEST_MEMBER_ID = 1L;
    public static final Long TEST_THEME_ID = 1L;
    public static final Long TEST_RESERVATION_ID = 1L;
    public static final Long TEST_TIME_ID = 1L;
    public static final LocalTime TEST_TIME = LocalTime.of(12, 0);
    
    // 테마 관련 상수
    public static final String TEST_THEME_NAME = "테마1";
    public static final String TEST_THEME_DESCRIPTION = "description";
    public static final String TEST_THEME_THUMBNAIL = "thumbnail";

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

    // 추가된 테스트 결과 객체 생성 메서드
    public static MemberResult createMemberResult() {
        return new MemberResult(TEST_MEMBER_ID, TEST_NAME, MemberRole.USER, TEST_EMAIL);
    }
    
    public static MemberResult createMemberResult(Long id, String name, MemberRole role, String email) {
        return new MemberResult(id, name, role, email);
    }
    
    public static ThemeResult createThemeResult() {
        return new ThemeResult(TEST_THEME_ID, "테마명", "테마 설명", "thumbnail.jpg");
    }
    
    public static ThemeResult createThemeResult(Long id, String name, String description, String thumbnail) {
        return new ThemeResult(id, name, description, thumbnail);
    }
    
    public static ReservationTimeResult createReservationTimeResult() {
        return new ReservationTimeResult(TEST_TIME_ID, TEST_TIME);
    }
    
    public static ReservationResult createReservationResult() {
        MemberResult memberResult = createMemberResult();
        ReservationTimeResult timeResult = createReservationTimeResult();
        ThemeResult themeResult = createThemeResult();
        
        return new ReservationResult(
                TEST_RESERVATION_ID,
                memberResult,
                TEST_DATE,
                timeResult,
                themeResult,
                ReservationStatus.RESERVED
        );
    }

    public static String createLoginJson() {
        return String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, TEST_EMAIL, TEST_PASSWORD);
    }

    public static String createReservationJson() {
        return String.format("""
                {
                    "date": "%s",
                    "themeId": %d,
                    "timeId": %d,
                    "memberId": %d
                }
                """, TEST_DATE, TEST_THEME_ID, TEST_TIME_ID, TEST_MEMBER_ID);
    }
    
    public static String createSignupJson() {
        return String.format("""
                {
                    "email": "%s",
                    "password": "%s",
                    "name": "%s"
                }
                """, TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
    }
    
    public static String createThemeJson() {
        return String.format("""
                {
                    "name": "%s",
                    "description": "%s",
                    "thumbnail": "%s"
                }
                """, TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
    }
}
