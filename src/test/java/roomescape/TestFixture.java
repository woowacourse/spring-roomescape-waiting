package roomescape;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;
import roomescape.domain.*;
import roomescape.service.result.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TestFixture {
    //인증 관련 상수
    public static final String AUTH_COOKIE_NAME = "token";
    public static final String VALID_TOKEN = "header.payload.signature";

    //멤버 관련 상수
    public static final Long TEST_MEMBER_ID = 1L;
    public static final String TEST_EMAIL = "test@email.com";
    public static final String TEST_PASSWORD = "password";
    public static final String TEST_NAME = "test";

    //예약 관련 상수
    public static final Long TEST_RESERVATION_ID = 1L;
    public static final LocalDate TEST_DATE = LocalDate.of(2025, 1, 1);

    //예약 시간 관련 상수
    public static final Long TEST_TIME_ID = 1L;
    public static final LocalTime TEST_TIME = LocalTime.of(12, 0);

    //테마 관련 상수
    public static final Long TEST_THEME_ID = 1L;
    public static final String TEST_THEME_NAME = "테마1";
    public static final String TEST_THEME_DESCRIPTION = "description";
    public static final String TEST_THEME_THUMBNAIL = "thumbnail";

    //예약 대기 관련 상수
    public static final Long TEST_WAITING_ID = 1L;

    //객체 생성 메서드
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

    public static MemberResult createMemberResult() {
        return new MemberResult(TEST_MEMBER_ID, TEST_NAME, MemberRole.USER, TEST_EMAIL);
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
    
    public static ReservationResult createDefaultReservationResult() {
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

    public static Waiting createDefaultWaiting() {
        return Waiting.createNew(TestFixture.createDefaultMember(),
                TestFixture.TEST_DATE,
                TestFixture.createDefaultReservationTime(),
                TestFixture.createDefaultTheme());
    }

    public static WaitingResult createDefaultWatiingResult() {
        return WaitingResult.from(createDefaultWaiting());
    }


    //JSON 생성 메서드
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

    //인기 테마 조회 셋업 메서드
    public static List<Theme> setupThemeRankTestCaseByLimit(EntityManager em) {
        Theme theme1 = createThemeByName("theme1");
        Theme theme2 = createThemeByName("theme2");
        Theme theme3 = createThemeByName("theme3");
        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);
        
        Member member = createDefaultMember();
        em.persist(member);
        ReservationTime time = createDefaultReservationTime();
        em.persist(time);
        
        // theme1에 2개의 예약
        Reservation reservation1 = createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme1);
        Reservation reservation2 = createDefaultReservation(member, LocalDate.of(2025, 1, 2), time, theme1);
        em.persist(reservation1);
        em.persist(reservation2);
        
        // theme2에 1개의 예약
        Reservation reservation3 = createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme2);
        em.persist(reservation3);
        
        return Arrays.asList(theme1, theme2, theme3);
    }
    
    public static List<Theme> setupThemeRankTestCaseByDateRange(EntityManager em) {
        Theme theme1 = createThemeByName("theme1");
        Theme theme2 = createThemeByName("theme2");
        Theme theme3 = createThemeByName("theme3");
        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);
        
        Member member = createDefaultMember();
        em.persist(member);
        ReservationTime time = createDefaultReservationTime();
        em.persist(time);
        
        // theme1에 3개의 예약 (다양한 날짜에)
        Reservation reservation1 = createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme1);
        Reservation reservation2 = createDefaultReservation(member, LocalDate.of(2025, 1, 2), time, theme1);
        Reservation reservation3 = createDefaultReservation(member, LocalDate.of(2025, 1, 3), time, theme1);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);
        
        // theme2에 1개의 예약
        Reservation reservation4 = createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme2);
        em.persist(reservation4);
        
        return Arrays.asList(theme1, theme2, theme3);
    }
}
