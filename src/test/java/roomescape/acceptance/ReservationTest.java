package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationTest extends AcceptanceTest{

    private String accessToken;

    @BeforeEach
    void insert() {
        TokenRequest tokenRequest = new TokenRequest("password", "admin@email.com");
        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");


        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationTimeRequest)
                .post("/times")
                .then().log().all()
                .extract();

        ThemeRequest themeRequest = new ThemeRequest("hi", "happy", "abcd.html");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(themeRequest)
                .post("/themes");

        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2030, 12, 12), 1L, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationRequest)
                .post("/reservations");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("예약 추가 API 테스트")
    @Test
    void createReservation() {
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2099, 12, 12), 1L, 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(2));

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("예약 조회 API 테스트")
    @Test
    void getReservations() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("예약 취소 API 테스트")
    @Test
    void deleteReservation() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("올바르지 않은 날짜 형식으로 입력시 예외처리")
    @Test
    void invalidDateFormat() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "1234567890");
        reservationRequest.put("date", "2025-aa-bb");
        reservationRequest.put("timeId", "1");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("올바르지 않은 예약자명 형식으로 입력시 예외처리")
    @Test
    void invalidTimeIdFormat() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "12345678900");
        reservationRequest.put("date", "2030-12-12");
        reservationRequest.put("timeId", "a");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("지나간 시점에 대한 예약시 예외처리")
    @Test
    void pastTimeSlotReservation() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "1234567890");
        reservationRequest.put("date", "1999-12-12");
        reservationRequest.put("timeId", "1");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("동일한 날짜와 시간에 중복 예약시 예외 처리")
    @Test
    void duplicateReservation() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "1234567890");
        reservationRequest.put("date", "2030-12-12");
        reservationRequest.put("timeId", "1");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
