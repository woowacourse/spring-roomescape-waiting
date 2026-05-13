package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserReservationTest {

    @Test
    void 사용자_예약__API() {
        Map<String, String> themes = new HashMap<>();
        themes.put("name", "무서운 이야기");
        themes.put("description", "공포");
        themes.put("url", "http://example.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themes)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, String> times = new HashMap<>();
        times.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(times)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> reservations = new HashMap<>();
        reservations.put("name", "브라운");
        reservations.put("date", "2026-08-04");
        reservations.put("timeId", "1");
        reservations.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 예약_정상_흐름_테스트() {
        Map<String, String> themes = new HashMap<>();
        themes.put("name", "무서운 이야기");
        themes.put("description", "공포");
        themes.put("url", "http://example.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themes)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, String> times1 = new HashMap<>();
        times1.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(times1)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> times2 = new HashMap<>();
        times2.put("startAt", "11:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(times2)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times/available?themeId=1&date=2026-06-04")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));

        Map<String, String> reservations = new HashMap<>();
        reservations.put("name", "브라운");
        reservations.put("date", "2026-06-04");
        reservations.put("timeId", "1");
        reservations.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times/available?themeId=1&date=2026-06-04")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 예약_날짜_시간_변경() {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", "무서운 이야기");
        theme.put("description", "공포");
        theme.put("url", "http://example.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, String> time1 = new HashMap<>();
        time1.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time1)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, String> time2 = new HashMap<>();
        time2.put("startAt", "11:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time2)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2026-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> update = new HashMap<>();
        update.put("name", "브라운");
        update.put("date", "2026-08-06");
        update.put("timeId", 2);
        update.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(update)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(200);
    }
}
