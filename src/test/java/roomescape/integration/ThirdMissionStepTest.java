package roomescape.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;

@SpringWebTest
public class ThirdMissionStepTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    void 시간_관리_API() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약과_시간_연결() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "brown");
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        Map<String, Object> theme = new HashMap<>();
        theme.put("name", "우아한 테마");
        theme.put("description", "우아한테크코스 전용 테마입니다.");
        theme.put("thumbnailUrl", "https://example.com/image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"startAt\": \"10:00\"}")
                .when().post("/admin/times")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 이름이_비어있으면_예약_생성_실패() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("date", "2026-04-29");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 이름이_공백이면_예약_생성_실패() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", " ");
        params.put("date", "2026-04-29");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 날짜가_없으면_예약_생성_실패() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "gildong");
        params.put("timeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 잘못된_시간_형식으로_시간_생성_실패() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "오전 10시");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 존재하지_않는_시간_ID로_예약_생성_실패() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "brown");
        reservation.put("date", "2023-08-05");
        reservation.put("timeId", 999);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 중복된_날짜와_시간으로_예약_생성_실패() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"startAt\": \"10:00\"}")
                .when().post("/admin/times")
                .then().statusCode(201);

        Map<String, Object> theme = new HashMap<>();
        theme.put("name", "우아한 테마");
        theme.put("description", "우아한테크코스 전용 테마입니다.");
        theme.put("thumbnailUrl", "https://example.com/image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "brown");
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }
}
