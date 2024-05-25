package roomescape.reservation.controller;

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
import java.util.Objects;
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
import roomescape.reservation.dto.ReservationResponse;
import roomescape.waiting.dto.WaitingCreateRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("예약을 조회, 추가, 삭제할 수 있다.")
    @Test
    void findCreateDeleteReservations() {
        Cookies cookies = RestAssuredTemplate.makeUserCookie(MEMBER_BRI);
        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = RestAssuredTemplate.create(ThemeFixture.toThemeCreateRequest(THEME_1), cookies).id();
        Long timeId = RestAssuredTemplate.create(TimeFixture.toTimeCreateRequest(TIME_1), cookies).id();

        ReservationCreateRequest params = new ReservationCreateRequest(null, date, timeId, themeId);

        // 예약 추가
        ReservationResponse response = RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getObject("", ReservationResponse.class);

        // 예약 조회
        List<ReservationResponse> reservationResponses = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("", ReservationResponse.class);

        assertThat(reservationResponses).containsExactlyInAnyOrder(response);

        // 예약 삭제
        RestAssured.given().log().all()
                .when().delete("/reservations/" + response.id())
                .then().log().all()
                .statusCode(204);

        // 예약 조회
        reservationResponses = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("", ReservationResponse.class);

        assertThat(reservationResponses).isEmpty();
    }

    @DisplayName("예약 추가 시 인자 중 null이 있을 경우, 예약을 추가할 수 없다.")
    @Test
    void createReservation_whenNameIsNull() {
        Cookies cookies = RestAssuredTemplate.makeUserCookie(MEMBER_ADMIN);
        Long themeId = RestAssuredTemplate.create(ThemeFixture.toThemeCreateRequest(THEME_1), cookies).id();
        Long timeId = RestAssuredTemplate.create(TimeFixture.toTimeCreateRequest(TIME_1), cookies).id();

        ReservationCreateRequest params = new ReservationCreateRequest
                (null, null, timeId, themeId);
        Cookies userCookies = CookieProvider.makeUserCookie();

        RestAssured.given().log().all()
                .cookies(userCookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", is("인자 중 null 값이 존재합니다."));
    }

    @DisplayName("예약 삭제 시 예약 대기가 존재하지 않는다면 삭제된다.")
    @Test
    void deleteReservation_whenWaitingNotExists() {
        Cookies cookies = RestAssuredTemplate.makeUserCookie(MEMBER_ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = RestAssuredTemplate.create(ThemeFixture.toThemeCreateRequest(THEME_1), cookies).id();
        Long timeId = RestAssuredTemplate.create(TimeFixture.toTimeCreateRequest(TIME_1), cookies).id();

        ReservationCreateRequest reservationParams =
                new ReservationCreateRequest(MEMBER_ADMIN.getId(), date, timeId, themeId);
        ReservationResponse response = RestAssuredTemplate.create(reservationParams, cookies);

        // 예약 삭제
        RestAssured.given().log().all()
                .when().delete("/reservations/" + response.id())
                .then().log().all()
                .statusCode(204);

        // 예약 조회
        List<ReservationResponse> reservationResponses = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList("", ReservationResponse.class);

        assertThat(reservationResponses).doesNotContain(response);
    }

    @DisplayName("예약 삭제 시 예약 대기가 존재한다면 첫번째 예약 대기가 예약으로 승격된다.")
    @Test
    void deleteReservation_whenWaitingExists() {
        Cookies adminCookies = RestAssuredTemplate.makeUserCookie(MEMBER_ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = RestAssuredTemplate.create(ThemeFixture.toThemeCreateRequest(THEME_1), adminCookies).id();
        Long timeId = RestAssuredTemplate.create(TimeFixture.toTimeCreateRequest(TIME_1), adminCookies).id();

        // 예약 추가
        Member reservationMember = MEMBER_BRI;
        Cookies reservationMemberCookies = RestAssuredTemplate.makeUserCookie(reservationMember);
        ReservationCreateRequest reservationParams =
                new ReservationCreateRequest(reservationMember.getId(), date, timeId, themeId);
        ReservationResponse response = RestAssuredTemplate.create(reservationParams, reservationMemberCookies);

        // 대기 추가
        Member waitingMember = MEMBER_BROWN;
        Cookies waitingMemberCookies = RestAssuredTemplate.makeUserCookie(waitingMember);
        WaitingCreateRequest waitingParams = new WaitingCreateRequest(date, timeId, themeId);
        RestAssuredTemplate.create(waitingParams, waitingMemberCookies);

        // 예약 삭제
        RestAssured.given().log().all()
                .when().delete("/reservations/" + response.id())
                .then().log().all()
                .statusCode(204);

        // 예약 조회
        List<ReservationResponse> reservationResponses = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList("", ReservationResponse.class);

        ReservationResponse promptedReservation = reservationResponses.stream()
                .filter(reservationResponse -> Objects.equals(reservationResponse.id(), response.id()))
                .findAny()
                .get();

        assertThat(promptedReservation.member().id())
                .isEqualTo(MEMBER_BROWN.getId());
    }
}
