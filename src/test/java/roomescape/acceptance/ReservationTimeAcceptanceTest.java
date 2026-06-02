package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.exception.ReservationTimeErrorCode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/empty.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationTimeAcceptanceTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 시간_생성() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 시간_조회() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/api/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 시간_삭제() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/api/admin/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 중복된_예약시간은_생성할_수_없다() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/admin/times")
                .then().log().all()
                .statusCode(
                        ReservationTimeErrorCode.RESERVATION_TIME_DUPLICATE.getHttpStatus().value())
                .body("code",
                        is(ReservationTimeErrorCode.RESERVATION_TIME_DUPLICATE.getErrorName()));
    }

    @Test
    void 정각이_아닌_예약시간은_생성할_수_없다() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:30");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/admin/times")
                .then().log().all()
                .statusCode(ReservationTimeErrorCode.INVALID_TIME.getHttpStatus().value())
                .body("code", is(ReservationTimeErrorCode.INVALID_TIME.getErrorName()));

    }

    @Test
    void 예약이_있는_시간은_삭제할_수_없다() {
        Map<String, String> timeParams = new HashMap<>();
        timeParams.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/api/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> themeParams = new HashMap<>();
        themeParams.put("name", "귀신찾기");
        themeParams.put("description", "귀신을 찾는 테마입니다.");
        themeParams.put("imageUrl", "https://image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservationParams = new HashMap<>();
        reservationParams.put("name", "브라운");
        reservationParams.put("date", "2026-08-05");
        reservationParams.put("timeId", 1);
        reservationParams.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/api/admin/times/1")
                .then().log().all()
                .statusCode(
                        ReservationTimeErrorCode.RESERVATION_EXIST_ON_TIME.getHttpStatus().value())
                .body("code",
                        is(ReservationTimeErrorCode.RESERVATION_EXIST_ON_TIME.getErrorName()));
    }
}
