package roomescape.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationTest {
    private static final String EMAIL = "testDB@email.com";
    private static final String PASSWORD = "1234";
    @LocalServerPort
    int port;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        accessToken = RestAssured
                .given().log().all()
                .body(new TokenRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @DisplayName("reservation 페이지 조회 요청이 올바르게 연결된다.")
    @Test
    void given_when_GetReservations_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(7)); // 아직 생성 요청이 없으니 Controller에서 임의로 넣어준 Reservation 갯수 만큼 검증하거나 0개임을 확인하세요.
    }

    @DisplayName("reservation 페이지에 새로운 예약 정보를 추가, 조회, 삭제할 수 있다.")
    @Test
    void given_when_saveAndDeleteReservations_then_statusCodeIsOkay() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2999-12-31");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(8));

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("등록되지 않은 시간으로 예약하는 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveNotExistTimeId_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2099-01-01");
        reservation.put("timeId", 500);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("부적절한 날짜로 예약하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", "", "2011-02-09"})
    void given_when_saveInvalidDate_then_statusCodeIsBadRequest(String invalidDate) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", invalidDate);
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 올바르지 않은 예약 날짜입니다."));
    }

    @DisplayName("부적절한 시간으로 예약하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", ""})
    void given_when_saveInvalidTimeId_then_statusCodeIsBadRequest(String invalidTimeId) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2999-12-31");
        reservation.put("timeId", invalidTimeId);
        reservation.put("themeId", 1);


        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 올바르지 않은 예약 시간입니다."));
    }

    @DisplayName("지나간 날짜와 시간으로 예약할 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveWithPastReservation_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().toString());
        reservation.put("timeId", 1); // 10:00
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 지나간 날짜와 시간으로 예약할 수 없습니다"));
    }

    @DisplayName("이미 예약이 된 시간을 등록하려 하면 400 오류를 반환한다.")
    @Test
    void given_when_saveDuplicatedReservation_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "포케");
        reservation.put("date", "2099-04-30");
        reservation.put("timeId", 1); // 10:00
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 예약이 종료되었습니다"));
    }

    @DisplayName("부적절한 테마로 예약하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", ""})
    void given_when_saveInvalidThemeId_then_statusCodeIsBadRequest(String invalidThemeId) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2999-04-01");
        reservation.put("timeId", 1);
        reservation.put("themeId", invalidThemeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 올바르지 않은 테마 입니다."));
    }

    @DisplayName("등록되지 않은 시간으로 예약하는 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveNotExistThemeId_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2099-01-01");
        reservation.put("timeId", 1);
        reservation.put("themeId", 99);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("[ERROR] 존재하지 않는 테마 입니다"));
    }

    @DisplayName("이미 예약이 된 시간의 다른 테마를 예약을 할 수 있다.")
    @Test
    void given_when_saveDuplicatedReservationDateAndTimeAndDifferentThemeId_then_statusCodeIsCreated() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "포케");
        reservation.put("date", "2099-04-30");
        reservation.put("timeId", 1); // 10:00
        reservation.put("themeId", 3);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", accessToken)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }
}
