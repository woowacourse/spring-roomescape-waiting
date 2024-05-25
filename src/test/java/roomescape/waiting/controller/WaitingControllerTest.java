package roomescape.waiting.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.fixture.MemberFixture.MEMBER_ADMIN;
import static roomescape.fixture.MemberFixture.MEMBER_BRI;
import static roomescape.fixture.MemberFixture.MEMBER_BROWN;
import static roomescape.fixture.ThemeFixture.THEME_1;
import static roomescape.fixture.TimeFixture.TIME_1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.fixture.CookieProvider;
import roomescape.fixture.RestAssuredTemplate;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.TimeFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.waiting.dto.WaitingCreateRequest;
import roomescape.waiting.dto.WaitingResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitingControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("예약 대기를 조회, 추가, 삭제 할 수 있다.")
    @Test
    void findCreateDeleteWaitingsTest() {
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

        // 대기 추가
        Member waitingMember = MEMBER_BROWN;
        Cookies waitingMemberCookies = RestAssuredTemplate.makeUserCookie(waitingMember);
        WaitingCreateRequest params = new WaitingCreateRequest(date, themeId, timeId);
        WaitingResponse response = RestAssured.given().log().all()
                .cookies(waitingMemberCookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getObject("", WaitingResponse.class);

        // 대기 조회
        List<WaitingResponse> waitingResponses = RestAssured.given().log().all()
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("", WaitingResponse.class);

        assertThat(waitingResponses).containsExactlyInAnyOrder(response);

        // 예약 삭제
        RestAssured.given().log().all()
                .when().delete("/waitings/" + response.id())
                .then().log().all()
                .statusCode(204);

        // 예약 조회
        waitingResponses = RestAssured.given().log().all()
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("", WaitingResponse.class);

        assertThat(waitingResponses).isEmpty();
    }

    @DisplayName("자신이 예약한 방탈출에 대해 예약 대기를 할 수 없다.")
    @Test
    void createWaiting_whenAlreadyReserve() {
        Cookies cookies = RestAssuredTemplate.makeUserCookie(MEMBER_ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = RestAssuredTemplate.create(ThemeFixture.toThemeCreateRequest(THEME_1), cookies).id();
        Long timeId = RestAssuredTemplate.create(TimeFixture.toTimeCreateRequest(TIME_1), cookies).id();

        // 예약 추가
        ReservationCreateRequest reservationParams =
                new ReservationCreateRequest(MEMBER_ADMIN.getId(), date, timeId, themeId);
        RestAssuredTemplate.create(reservationParams, cookies);

        // 대기 추가
        WaitingCreateRequest waitingParams = new WaitingCreateRequest(date, themeId, timeId);
        RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", is("자신이 예약한 방탈출에 대해 예약 대기를 할 수 없습니다."));
    }

    @DisplayName("자신이 예약 대기한 방탈출에 대해 예약 대기를 할 수 없다.")
    @Test
    void createWaiting_whenDuplicateWaiting() {
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

        // 대기 추가
        Member waitingMember = MEMBER_BROWN;
        Cookies waitingMemberCookies = RestAssuredTemplate.makeUserCookie(waitingMember);
        WaitingCreateRequest params = new WaitingCreateRequest(date, themeId, timeId);
        RestAssured.given().log().all()
                .cookies(waitingMemberCookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getObject("", WaitingResponse.class);

        // 대기 중복 추가
        RestAssured.given().log().all()
                .cookies(waitingMemberCookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", is("중복으로 예약 대기를 할 수 없습니다."));
    }

    @DisplayName("존재하지 않는 예약에 대해 예약 대기를 할 수 없다.")
    @Test
    void createWaiting_whenNotExistReservation() {
        WaitingCreateRequest waitingParams = new WaitingCreateRequest(
                LocalDate.of(2040, 8, 5), 1L, 1L);
        long expectedId = 1L;
        Cookies userCookies = CookieProvider.makeUserCookie();

        RestAssured.given().log().all()
                .cookies(userCookies)
                .contentType(ContentType.JSON)
                .body(waitingParams)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", is("존재하지 않는 예약에 대해 대기할 수 없습니다."));
    }
}
