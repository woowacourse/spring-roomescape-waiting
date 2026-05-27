package roomescape.reservation.controller;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약 생성 성공")
    void 예약_생성_성공() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "date", "2099-08-05", "timeId", 1, "themeId", 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("현미밥"))
                .body("date", equalTo("2099-08-05"))
                .body("themeId", equalTo(1));
    }

    @Test
    @DisplayName("과거 날짜로 예약 생성 시 400")
    void 과거_날짜_예약_생성_실패() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "date", "2020-01-01", "timeId", 1, "themeId", 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", equalTo("PAST_TIME_CREATE"));
    }

    @Test
    @DisplayName("중복 예약 생성 시 409")
    void 중복_예약_생성_실패() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "date", "2099-12-01", "timeId", 1, "themeId", 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("errorCode", equalTo("DUPLICATE_RESERVATION"));
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약 생성 시 404")
    void 존재하지_않는_timeId_예약_생성_실패() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "date", "2099-08-05", "timeId", 999, "themeId", 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", equalTo("TIME_NOT_FOUND"));
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약 생성 시 404")
    void 존재하지_않는_themeId_예약_생성_실패() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "date", "2099-08-05", "timeId", 1, "themeId", 999))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", equalTo("THEME_NOT_FOUND"));
    }

    @Test
    @DisplayName("예약 수정 성공")
    void 예약_수정_성공() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", "2099-12-02", "timeId", 2))
                .when().patch("/reservations/11")
                .then().log().all()
                .statusCode(200)
                .body("date", equalTo("2099-12-02"))
                .body("time.id", equalTo(2));
    }

    @Test
    @DisplayName("이미 지난 예약 수정 시 400")
    void 과거_예약_수정_실패() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", "2099-12-02", "timeId", 2))
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", equalTo("PAST_RESERVATION_UPDATE"));
    }

    @Test
    @DisplayName("예약 삭제 성공")
    void 예약_삭제_성공() {
        RestAssured.given().log().all()
                .when().delete("/reservations/11")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("이미 지난 예약 삭제 시 400")
    void 과거_예약_삭제_실패() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", equalTo("PAST_RESERVATION_CANCEL"));
    }

    @Test
    @DisplayName("예약 ID 조회")
    void 예약_ID_조회_성공() {
        RestAssured.given().log().all()
                .queryParam("date", "2099-12-01")
                .queryParam("themeId", 1)
                .queryParam("timeId", 1)
                .when().get("/reservations/id")
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(11));
    }
}