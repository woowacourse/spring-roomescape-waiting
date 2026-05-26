package roomescape.reservationtime.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
class ReservationTimeControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("시간 생성 성공")
    void 시간_생성_성공() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "20:00", "finishAt", "21:00"))
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo("20:00:00"));
    }

    @Test
    @DisplayName("시간 전체 조회 성공")
    void 시간_전체_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @Test
    @DisplayName("시간 삭제 성공")
    void 시간_삭제_성공() {
        Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", "20:00", "finishAt", "21:00"))
                .when().post("/times")
                .then().extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/times/" + id)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("예약 가능 시간 조회 성공")
    void 예약_가능_시간_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/times/available?date=2026-05-10&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}