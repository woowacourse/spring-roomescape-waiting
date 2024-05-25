package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_ADMIN;
import static roomescape.fixture.MemberFixture.MEMBER_BRI;
import static roomescape.fixture.MemberFixture.MEMBER_BROWN;
import static roomescape.fixture.ThemeFixture.THEME_1;
import static roomescape.fixture.TimeFixture.TIME_1;

import io.restassured.RestAssured;
import io.restassured.http.Cookies;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.fixture.RestAssuredTemplate;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.TimeFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.waiting.dto.WaitingCreateRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MyReservationControllerTest {
    private static final int BRI_COUNT_OF_RESERVATION = 1;
    private static final int BRI_COUNT_OF_WAITING = 1;


    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("로그인한 사용자의 예약 및 예약 대기 목록을 읽을 수 있다.")
    @Test
    void findMyReservationsWaitings() {
        Cookies adminCookies = RestAssuredTemplate.makeUserCookie(MEMBER_ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = RestAssuredTemplate.create(ThemeFixture.toThemeCreateRequest(THEME_1), adminCookies).id();
        Long timeId = RestAssuredTemplate.create(TimeFixture.toTimeCreateRequest(TIME_1), adminCookies).id();

        // 예약 추가
        Member reservationMember = MEMBER_BRI;
        Cookies reservationMemberCookies = RestAssuredTemplate.makeUserCookie(reservationMember);
        ReservationCreateRequest reservationParams =
                new ReservationCreateRequest(reservationMember.getId(), date, timeId, themeId);
        RestAssuredTemplate.create(reservationParams, reservationMemberCookies);

        // 로그인 유저 대기 추가
        Member loginMember = MEMBER_BROWN;
        Cookies loginMemberCookies = RestAssuredTemplate.makeUserCookie(loginMember);
        WaitingCreateRequest waitingParams = new WaitingCreateRequest(date, timeId, themeId);
        RestAssuredTemplate.create(waitingParams, loginMemberCookies);

        // 로그인 유저 예약 추가
        reservationParams = new ReservationCreateRequest(reservationMember.getId(), date.plusDays(1), timeId, themeId);
        RestAssuredTemplate.create(reservationParams, loginMemberCookies);

        // 나의 예약 목록 조회
        int size = RestAssured.given().log().all()
                .cookies(loginMemberCookies)
                .when().get("/my/reservaitons")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        assertThat(size).isEqualTo(2);
    }
}
