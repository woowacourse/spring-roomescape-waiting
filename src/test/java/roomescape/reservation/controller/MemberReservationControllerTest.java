package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.service.TokenCookieService;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("사용자 예약 컨트롤러")
class MemberReservationControllerTest {

    @LocalServerPort
    private int port;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        Map<String, String> body = new HashMap<>();
        body.put("email", "test@gmail.com");
        body.put("password", "password");

        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.SET_COOKIE)
                .split(";")[0]
                .split(TokenCookieService.COOKIE_TOKEN_KEY + "=")[1];
    }

    ValidatableResponse makeReservation(MemberReservationCreateRequest request) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(request)
                .queryParam("type", "member")
                .when().post("/reservations")
                .then().log().all();
    }

    ValidatableResponse makeWaiting(MemberReservationCreateRequest request) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(request)
                .queryParam("type", "member")
                .when().post("/waitings")
                .then().log().all();
    }

    @DisplayName("사용자 예약 컨트롤러는 /reservation으로 GET 요청이 들어오면 사용자 예약 페이지를 반환한다.")
    @Test
    void readUserReservation() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200)
                .body("html.body.div.h2", equalTo("예약 페이지"));
    }

    @DisplayName("사용자 예약 컨트롤러는 예약 생성 시 생성된 값을 반환한다.")
    @Test
    void createMemberReservation() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), 1L, 1L);

        // when
        String name = makeReservation(request)
                .extract()
                .body()
                .jsonPath().get("member.name");

        // then
        assertThat(name).isEqualTo("클로버");
    }

    @DisplayName("사용자 예약 컨트롤러는 로그인하지 않은 사용자가 예약을 생성하는 경우 예외가 발생한다.")
    @Test
    void createMemberReservationWithOutLogin() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), 1L, 1L);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .queryParam("type", "member")
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("사용자 예약 컨트롤러는 잘못된 형식의 날짜로 예약 생성 요청 시 400을 응답한다.")
    @ValueSource(strings = {"Hello", "2024-13-20", "2900-12-32"})
    @ParameterizedTest
    void createInvalidDateReservation(String invalidString) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", invalidString);
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("사용자 예약 컨트롤러는 지난 날짜로 예약 생성 요청 시 400을 응답한다.")
    @Test
    void createReservationWithBeforeDate() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("1999-05-27"), 1L, 1L);

        // when, then
        makeReservation(request).statusCode(400);
    }

    @DisplayName("사용자 예약 컨트롤러는 중복 예약 생성 요청 시 400을 응답한다.")
    @Test
    void createReservationWithDuplicated() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), 1L, 1L);
        makeReservation(request).statusCode(200);

        // when, then
        makeReservation(request).statusCode(400);
    }

    @DisplayName("사용자 예약 컨트롤러는 존재하지 않는 시간으로 예약 생성을 요청할 경우 404를 응답한다.")
    @Test
    void createReservationWithNonExistsTime() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), Long.MAX_VALUE, 1L);

        // when, then
        makeReservation(request).statusCode(404);
    }

    @DisplayName("사용자 예약 컨트롤러는 존재하지 않는 테마로 예약 생성을 요청할 경우 404를 응답한다.")
    @Test
    void createReservationWithNonExistsTheme() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), 1L, Long.MAX_VALUE);

        // when, then
        makeReservation(request).statusCode(404);
    }

    @DisplayName("사용자 예약 컨트롤러는 예약 생성 시 잘못된 형식의 본문이 들어오면 400을 응답한다.")
    @Test
    void createInvalidRequestBody() {
        // given
        String invalidBody = "invalidBody";

        // when, then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(invalidBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("내 예약 조회 요청 시 본인의 예약만 응답한다.")
    @Test
    void readMemberReservations() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("사용자 예약 대기 컨트롤러는 예약 대기 생성 시 생성된 값을 반환한다.")
    @Test
    void createWaitingReservation() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), 1L, 1L);
        makeReservation(request);

        // when
        String name = makeWaiting(request)
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().get("member.name");

        // then
        assertThat(name).isEqualTo("클로버");
    }

    @DisplayName("기 예약된 내역이 없는 경우에 예약 대기 시 400 코드를 반환한다.")
    @Test
    void createWaitingReservationWithoutReservation() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), 1L, 1L);

        // when, then
        makeWaiting(request).statusCode(400);
    }

    @DisplayName("사용자 예약 대기 컨트롤러는 중복 예약 대기 생성 시 400을 반환한다.")
    @Test
    void createDuplicatedWaitingReservation() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), 1L, 1L);
        makeReservation(request);
        makeWaiting(request);

        // when
        makeWaiting(request).statusCode(400);
    }

    @DisplayName("사용자 본인의 예약 대기가 아니면 삭제할 수 없다.")
    @Test
    void deleteNotSelfWaitingReservation() {
        // given
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest(LocalDate.parse("2099-05-27"), 1L, 1L);
        makeReservation(request);

        Long id = makeWaiting(request)
                .statusCode(200)
                .extract()
                .jsonPath().getLong("id");

        // when
        Map<String, String> body = new HashMap<>();
        body.put("email", "test2@gmail.com");
        body.put("password", "password");

        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.SET_COOKIE)
                .split(";")[0]
                .split(TokenCookieService.COOKIE_TOKEN_KEY + "=")[1];

        // then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .queryParam("type", "member")
                .when().delete("/waitings/" + id)
                .then().log().all()
                .statusCode(401);
    }
}
