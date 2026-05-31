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
import roomescape.exception.WaitingErrorCode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/empty.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class WaitingAcceptanceTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 대기를_생성한다() {
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
        themeParams.put("description", "귀신찾기을 찾는 테마입니다.");
        themeParams.put("imageUrl", "https://image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "코코");
        waiting.put("date", "2026-08-05");
        waiting.put("timeId", 1L);
        waiting.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/api/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/api/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].name", is("코코"))
                .body("[0].waitingNumber", is(1));
    }

    @Test
    void 이름으로_대기를_조회할_수_있다() {
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
        themeParams.put("description", "귀신찾기을 찾는 테마입니다.");
        themeParams.put("imageUrl", "https://image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "코코");
        waiting.put("date", "2026-08-05");
        waiting.put("timeId", 1L);
        waiting.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/api/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .queryParam("name", "코코")
                .when().get("/api/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("코코"))
                .body("[0].waitingNumber", is(1));
    }

    @Test
    void 대기id로_대기를_삭제한다() {
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
        themeParams.put("description", "귀신찾기을 찾는 테마입니다.");
        themeParams.put("imageUrl", "https://image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "코코");
        waiting.put("date", "2026-08-05");
        waiting.put("timeId", 1L);
        waiting.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/api/waitings")
                .then().log().all()
                .statusCode(201);

        Long waitingId = RestAssured.given().log().all()
                .when().get("/api/admin/waitings")
                .then().log().all()
                .extract().jsonPath().getLong("[0].id");

        RestAssured.given().log().all()
                .when().delete("/api/waitings/" + waitingId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/api/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 대기_등록할_예약이_존재하지_않으면_대기를_생성할_수_없고_즉시_예약할_수_있다() {
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
        themeParams.put("description", "귀신찾기을 찾는 테마입니다.");
        themeParams.put("imageUrl", "https://image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "코코");
        waiting.put("date", "2026-08-05");
        waiting.put("timeId", 1L);
        waiting.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/api/waitings")
                .then().log().all()
                .statusCode(
                        WaitingErrorCode.IMMEDIATE_RESERVATION_AVAILABLE.getHttpStatus().value())
                .body("code", is(WaitingErrorCode.IMMEDIATE_RESERVATION_AVAILABLE.getErrorName()));
    }

    @Test
    void 내_예약에는_대기를_생성할_수_없다() {
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
        themeParams.put("description", "귀신찾기을 찾는 테마입니다.");
        themeParams.put("imageUrl", "https://image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "브라운");
        waiting.put("date", "2026-08-05");
        waiting.put("timeId", 1L);
        waiting.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/api/waitings")
                .then().log().all()
                .statusCode(WaitingErrorCode.CANNOT_WAITLIST_CONFIRMED_SLOT.getHttpStatus().value())
                .body("code", is(WaitingErrorCode.CANNOT_WAITLIST_CONFIRMED_SLOT.getErrorName()));
    }

    @Test
    void 같은_이름으로_동일한_날짜_시간_테마에_하나의_대기만_생성할_수_있다() {
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
        themeParams.put("description", "귀신찾기을 찾는 테마입니다.");
        themeParams.put("imageUrl", "https://image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1L);
        reservation.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("name", "코코");
        waiting.put("date", "2026-08-05");
        waiting.put("timeId", 1L);
        waiting.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/api/waitings")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> duplicatedWaiting = new HashMap<>();
        waiting.put("name", "코코");
        waiting.put("date", "2026-08-05");
        waiting.put("timeId", 1L);
        waiting.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waiting)
                .when().post("/api/waitings")
                .then().log().all()
                .statusCode(WaitingErrorCode.WAITING_DUPLICATE.getHttpStatus().value())
                .body("code", is(WaitingErrorCode.WAITING_DUPLICATE.getErrorName()));
    }
}
