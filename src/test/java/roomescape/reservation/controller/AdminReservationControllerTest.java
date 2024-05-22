package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.service.TokenCookieService;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("어드민 예약 컨트롤러")
class AdminReservationControllerTest {

    @LocalServerPort
    private int port;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        Map<String, String> body = new HashMap<>();
        body.put("email", "admin@gmail.com");
        body.put("password", "password");

        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(body)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.SET_COOKIE)
                .split(";")[0]
                .split(TokenCookieService.COOKIE_TOKEN_KEY + "=")[1];
    }

    @DisplayName("어드민 예약 컨트롤러는 예약 조회 시 200을 응답한다.")
    @Test
    void readReservations() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(11));
    }

    @DisplayName("어드민 예약 컨트롤러는 예약 대기 조회 시 200을 응답한다.")
    @Test
    void readWaitingReservations() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @DisplayName("어드민 예약 컨트롤러는 기간, 사용자, 테마로 예약 조회 시 200을 응답한다.")
    @Test
    void searchReservations() {
        RestAssured.given().log().all()
                .queryParam("dateFrom", "2024-01-01")
                .queryParam("dateTo", "2099-12-31")
                .queryParam("memberId", 1)
                .queryParam("themeId", 2)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/reservations/search")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("어드민 예약 컨트롤러는 예약 생성 시 200을 응답한다.")
    @Test
    void createReservation() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("memberId", 1);
        reservation.put("date", LocalDate.MAX.toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("status", ReservationStatus.CONFIRMATION);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/reservations")
                .then()
                .statusCode(200)
                .body("size()", is(11));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(200);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/reservations")
                .then()
                .statusCode(200)
                .body("size()", is(12));
    }

    @DisplayName("어드민 예약 컨트롤러는 사용자 id 없이 예약을 생성할 경우 400을 응답한다.")
    @Test
    void createReservationWithoutMemberId() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", LocalDate.MAX.toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("status", ReservationStatus.CONFIRMATION);

        // when
        String detailMessage = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        // then
        assertThat(detailMessage).isEqualTo("사용자는 비어있을 수 없습니다.");
    }

    @DisplayName("어드민 예약 컨트롤러는 id 값에 따라 예약시 204을 응답한다.")
    @Test
    void deleteReservation() {
        RestAssured.given()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/reservations")
                .then()
                .statusCode(200)
                .body("size()", is(11));

        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/reservations")
                .then()
                .statusCode(200)
                .body("size()", is(10));
    }

    @DisplayName("어드민 예약 컨트롤러는 예약 대기 승인시 200을 응답한다.")
    @Test
    void confirmWaitingReservation() {
        // given
        long id = 11L;

        // when & then
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().put("/admin/reservations/waiting/" + id)
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("어드민 예약 컨트롤러는 대기 상태가 아닌 예약을 승인할 경우 400을 응답한다.")
    @Test
    void confirmNotWaitingReservation() {
        // given
        long id = 1L;

        // when
        String detailMessage = RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().put("/admin/reservations/waiting/" + id)
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        // then
        assertThat(detailMessage).isEqualTo("해당 예약은 대기 상태가 아닙니다.");
    }

    @DisplayName("어드민 예약 컨트롤러는 첫 번째 예약 대기가 아닌 대기를 승인하려고 하면 400을 응답한다.")
    @Test
    void confirmNotFirstWaitingReservation() {
        // given
        long id = 12L;

        // when
        String detailMessage = RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().put("/admin/reservations/waiting/" + id)
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        // then
        assertThat(detailMessage).isEqualTo("예약 대기는 순서대로 승인할 수 있습니다.");
    }

    @DisplayName("어드민 예약 컨트롤러는 이미 예약이 존재하는 대기를 승인하려고 하는 경우 400을 응답한다.")
    @Test
    void confirmAlreadyConfirmReservation() {
        // given
        long id = 14L;

        // when
        String detailMessage = RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().put("/admin/reservations/waiting/" + id)
                .then().log().all()
                .statusCode(400)
                .extract()
                .jsonPath().get("detail");

        // then
        assertThat(detailMessage).isEqualTo("이미 예약이 존재해 대기를 승인할 수 없습니다.");
    }
}
