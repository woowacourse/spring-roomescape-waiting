package roomescape.domain.reservation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 예약_정상_생성_확인_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reservation WHERE name = ?", Integer.class, "유저1"
        );
        assertEquals(1, count);
    }

    @Test
    void createReservation_이름이_비어있는경우_에러_반환_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400);
    }

    @Test
    void createReservation_과거_날짜인경우_에러_반환_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2000-01-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(422);
    }

    @Test
    void createReservation_중복된_예약인경우_에러_반환_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        insertReservation("유저1", "2099-12-31", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저2");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(409);
    }

    @Test
    void createReservation_존재하지_않는_시간_id인경우_에러_반환_테스트() {
        Long themeId = insertTheme("테마1");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", 999L);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(404);
    }

    @Test
    void createReservation_존재하지_않는_테마_id인경우_에러_반환_테스트() {
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", 999L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(404);
    }

    @Test
    void 예약_가능_시간_조회_정상_동작_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId1 = insertTime("10:00", "11:00");
        Long timeId2 = insertTime("11:00", "12:00");
        Long timeId3 = insertTime("12:00", "13:00");
        insertReservation("유저1", "2099-12-31", timeId2, themeId);

        RestAssured.given().log().all()
            .when().get("/reservations?date=2099-12-31&themeId=" + themeId)
            .then().log().all()
            .statusCode(200)
            .body("size()", is(2))
            .body("id", containsInAnyOrder(timeId1.intValue(), timeId3.intValue()));
    }

    @Test
    void 예약_가능_시간_조회_예약이_없는경우_전체_시간_반환_테스트() {
        Long themeId = insertTheme("테마1");
        insertTime("10:00", "11:00");
        insertTime("11:00", "12:00");

        RestAssured.given().log().all()
            .when().get("/reservations?date=2099-12-31&themeId=" + themeId)
            .then().log().all()
            .statusCode(200)
            .body("size()", is(2));
    }

    @Test
    void 나의_예약_조회_정상_동작_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        insertReservation("유저1", "2099-12-31", timeId, themeId);
        insertReservation("유저1", "2099-12-30", timeId, themeId);
        insertReservation("유저2", "2099-12-29", timeId, themeId);

        RestAssured.given().log().all()
            .when().get("/reservations/mine?name=유저1")
            .then().log().all()
            .statusCode(200)
            .body("reservations.size()", is(2))
            .body("reservations[0].name", is("유저1"))
            .body("reservations[0].themeName", is("테마1"));
    }

    @Test
    void 나의_예약_조회_예약이_없는경우_빈_리스트_반환_테스트() {
        RestAssured.given().log().all()
            .when().get("/reservations/mine?name=없는유저")
            .then().log().all()
            .statusCode(200)
            .body("reservations", is(empty()));
    }

    @Test
    void 나의_예약_조회_이름이_100자를_초과하는경우_에러_반환_테스트() {
        String name = "a".repeat(101);

        RestAssured.given().log().all()
            .queryParam("name", name)
            .when().get("/reservations/mine")
            .then().log().all()
            .statusCode(400)
            .body("message", is("이름은 100자 이하여야 합니다."));
    }

    @Test
    void 특정_예약_삭제_정상_동작_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        Long reservationId = insertReservation("유저1", "2099-12-31", timeId, themeId);

        RestAssured.given().log().all()
            .when().delete("/reservations/" + reservationId + "?name=유저1")
            .then().log().all()
            .statusCode(204);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reservation WHERE id = ?", Integer.class, reservationId
        );
        assertEquals(0, count);
    }

    @Test
    void deleteReservation_존재하지_않는_id인경우_에러_반환_테스트() {
        RestAssured.given().log().all()
            .when().delete("/reservations/999?name=유저1")
            .then().log().all()
            .statusCode(404);
    }

    @Test
    void 나의_예약_수정_정상_동작_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId1 = insertTime("10:00", "11:00");
        Long timeId2 = insertTime("11:00", "12:00");
        Long reservationId = insertReservation("유저1", "2099-12-30", timeId1, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId2);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservation/" + reservationId)
            .then().log().all()
            .statusCode(204);

        String date = jdbcTemplate.queryForObject(
            "SELECT date FROM reservation WHERE id = ?", String.class, reservationId
        );
        Long updatedTimeId = jdbcTemplate.queryForObject(
            "SELECT time_id FROM reservation WHERE id = ?", Long.class, reservationId
        );
        assertAll(
            () -> assertEquals("2099-12-31", date),
            () -> assertEquals(timeId2, updatedTimeId)
        );
    }

    @Test
    void updateMyReservation_권한이_없는경우_에러_반환_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        Long reservationId = insertReservation("유저1", "2099-12-30", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "다른유저");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservation/" + reservationId)
            .then().log().all()
            .statusCode(401);
    }

    @Test
    void updateMyReservation_존재하지_않는_id인경우_에러_반환_테스트() {
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservation/999")
            .then().log().all()
            .statusCode(404);
    }

    @Test
    void updateMyReservation_존재하지_않는_시간_id인경우_에러_반환_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        Long reservationId = insertReservation("유저1", "2099-12-30", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", 999L);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservation/" + reservationId)
            .then().log().all()
            .statusCode(404);
    }

    @Test
    void updateMyReservation_과거_날짜인경우_에러_반환_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        Long reservationId = insertReservation("유저1", "2099-12-30", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2000-01-01");
        params.put("timeId", timeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().patch("/reservation/" + reservationId)
            .then().log().all()
            .statusCode(422);
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
            name, "설명", "https://example.com/image.jpg"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM theme WHERE name = ?", Long.class, name
        );
    }

    private Long insertTime(String startAt, String finishAt) {
        jdbcTemplate.update(
            "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
            startAt, finishAt
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt
        );
    }

    private Long insertReservation(String name, String date, Long timeId, Long themeId) {
        jdbcTemplate.update(
            "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
            name, date, timeId, themeId
        );
        return jdbcTemplate.queryForObject(
            "SELECT MAX(id) FROM reservation", Long.class
        );
    }
}
