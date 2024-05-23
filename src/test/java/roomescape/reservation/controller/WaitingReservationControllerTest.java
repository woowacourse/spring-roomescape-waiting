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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("사용자 예약 대기 컨트롤러")
class WaitingReservationControllerTest {

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
        makeReservation();
    }

    void makeReservation() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", LocalDate.MAX);
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .when().post("/reservations");
    }

    @DisplayName("사용자 예약 대기 컨트롤러는 예약 대기 생성 시 생성된 값을 반환한다.")
    @Test
    void createWaitingReservation() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", LocalDate.MAX.toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        // when
        String name = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .queryParam("type", "member")
                .when().post("/waitings")
                .then().log().all()
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
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2024-05-23");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        // when, then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .queryParam("type", "member")
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("사용자 예약 대기 컨트롤러는 중복 예약 대기 생성 시 400을 반환한다.")
    @Test
    void createDuplicatedWaitingReservation() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", LocalDate.MAX.toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .queryParam("type", "member")
                .when().post("/waitings")
                .then().log().all()
                .statusCode(200);

        // then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .queryParam("type", "member")
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("사용자 본인의 예약 대기가 아니면 삭제할 수 없다.")
    @Test
    void deleteNotSelfWaitingReservation() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", LocalDate.MAX.toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        Long id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .body(reservation)
                .queryParam("type", "member")
                .when().post("/waitings")
                .then().log().all()
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
