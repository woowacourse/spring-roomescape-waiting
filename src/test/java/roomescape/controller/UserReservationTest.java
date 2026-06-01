package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserReservationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("delete from slot");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");

        jdbcTemplate.update("alter table waiting alter column id restart with 1");
        jdbcTemplate.update("alter table reservation alter column id restart with 1");
        jdbcTemplate.update("alter table slot alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");
    }

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
                .body("size()", is(2))
                .body("findAll { it.isAvailable }.size()", is(1));
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

        String reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getString("id");

        Map<String, Object> update = new HashMap<>();
        update.put("name", "브라운");
        update.put("date", "2026-08-06");
        update.put("timeId", 2);
        update.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(update)
                .when().put("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 중복_예약시_409를_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("errorCode", is("DUPLICATE_RESERVATION"))
                .body("message", is("이미 예약된 시간입니다."));
    }

    @Test
    void 이미_예약된_시간으로_변경시_409를_반환한다() {
        createTheme();
        createTime("10:00");
        createTime("11:00");

        Map<String, Object> reservation1 = createReservationBody("브라운", "2026-08-05", 1, 1);
        String reservationId = RestAssured.given().contentType(ContentType.JSON).body(reservation1)
                .when().post("/reservations").then().statusCode(201)
                .extract().jsonPath().getString("id");

        Map<String, Object> reservation2 = createReservationBody("네오", "2026-08-05", 2, 1);
        RestAssured.given().contentType(ContentType.JSON).body(reservation2)
                .when().post("/reservations").then().statusCode(201);

        Map<String, Object> update = createReservationBody("브라운", "2026-08-05", 2, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(update)
                .when().put("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(409)
                .body("errorCode", is("DUPLICATE_RESERVATION"))
                .body("message", is("이미 예약된 시간입니다."));
    }

    @Test
    void 존재하지_않는_예약_변경시_404를_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> update = createReservationBody("브라운", "2026-08-05", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(update)
                .when().put("/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("RESERVATION_NOT_FOUND"))
                .body("message", is("999번 예약을 찾을 수 없습니다."));
    }

    @Test
    void 존재하지_않는_시간으로_예약시_404를_반환한다() {
        createTheme();

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 999, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("TIME_NOT_FOUND"))
                .body("message", is("999번 예약 시간을 찾을 수 없습니다."));
    }

    @Test
    void 존재하지_않는_테마로_예약시_404를_반환한다() {
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 999);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404)
                .body("errorCode", is("THEME_NOT_FOUND"))
                .body("message", is("999번 테마를 찾을 수 없습니다."));
    }

    @Test
    void 과거_날짜로_예약시_400을_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2020-01-01", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("INVALID_DATE_OR_TIME"))
                .body("message", is("이미 지난 날짜이거나 시간입니다."));
    }

    @Test
    void 과거_날짜로_변경시_400을_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("브라운", "2026-08-05", 1, 1);
        String reservationId = RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201)
                .extract().jsonPath().getString("id");

        Map<String, Object> update = createReservationBody("브라운", "2020-01-01", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(update)
                .when().put("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("INVALID_DATE_OR_TIME"))
                .body("message", is("이미 지난 날짜이거나 시간입니다."));
    }

    @Test
    void 빈_이름으로_예약시_400을_반환한다() {
        createTheme();
        createTime("10:00");

        Map<String, Object> reservation = createReservationBody("", "2026-08-05", 1, 1);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("INVALID_INPUT"))
                .body("message", is("[name] 필드가 비어있습니다."));
    }

    private void createTheme() {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", "무서운 이야기");
        theme.put("description", "공포");
        theme.put("url", "http://example.com");
        RestAssured.given().contentType(ContentType.JSON).body(theme)
                .when().post("/admin/themes").then().statusCode(201);
    }

    private void createTime(String startAt) {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", startAt);
        RestAssured.given().contentType(ContentType.JSON).body(time)
                .when().post("/admin/times").then().statusCode(201);
    }

    private Map<String, Object> createReservationBody(String name, String date, int timeId, int themeId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("date", date);
        body.put("timeId", timeId);
        body.put("themeId", themeId);
        return body;
    }
}
