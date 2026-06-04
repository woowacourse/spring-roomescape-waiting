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
    void 예약_정상_생성_확인_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("SUCCESS", result.get("status"));

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
                .statusCode(400)
                .body("message", is("예약할 사용자의 이름은 필수 입력 값입니다."));
    }

    @Test
    void createReservation_과거_날짜인경우_에러_반환_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2000-01-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("이미 지난 날짜의 예약은 생성할 수 없습니다.", result.get("errorMessage"));
    }

    @Test
    void createReservation_중복된_예약인경우_에러_반환_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        insertReservation("유저1", "2099-12-31", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저2");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("이미 선택된 예약입니다.", result.get("errorMessage"));
    }

    @Test
    void createReservation_존재하지_않는_시간_id인경우_에러_반환_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", 999L);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("해당 time id를 찾을 수 없습니다.", result.get("errorMessage"));
    }

    @Test
    void createReservation_존재하지_않는_테마_id인경우_에러_반환_테스트() throws InterruptedException {
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", 999L);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("해당 theme id를 찾을 수 없습니다.", result.get("errorMessage"));
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
    void 특정_예약_삭제_정상_동작_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        Long reservationId = insertReservation("유저1", "2099-12-31", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?", Integer.class, reservationId
        );
        assertEquals(0, count);
    }

    @Test
    void 예약_삭제_시_대기자가_있으면_첫번째_대기자가_예약으로_승격된다() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        Long reservationId = insertReservation("유저1", "2099-12-31", timeId, themeId);
        insertWaiting("대기자1", "2099-12-31", timeId, themeId);
        insertWaiting("대기자2", "2099-12-31", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        Integer reservationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE name = ? AND date = ?",
                Integer.class, "대기자1", "2099-12-31"
        );
        Integer waitingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE name = ? AND date = ?",
                Integer.class, "대기자1", "2099-12-31"
        );
        Integer remainingWaitingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE date = ?",
                Integer.class, "2099-12-31"
        );
        assertAll(
                () -> assertEquals(1, reservationCount),
                () -> assertEquals(0, waitingCount),
                () -> assertEquals(1, remainingWaitingCount)
        );
    }

    @Test
    void deleteReservation_존재하지_않는_id인경우_에러_반환_테스트() {
        RestAssured.given().log().all()
                .when().delete("/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("message", is("해당 reservation id를 찾을 수 없습니다."));
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
                .when().patch("/reservations/" + reservationId)
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
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(401)
                .body("message", is("해당 예약을 삭제할 권한이 없습니다."));
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
                .when().patch("/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("message", is("해당 reservation id를 찾을 수 없습니다."));
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
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(404)
                .body("message", is("해당 time id를 찾을 수 없습니다."));
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
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(422)
                .body("message", is("이미 지난 날짜의 예약은 생성할 수 없습니다."));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pollUntilDone(String jobId) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            Map<String, Object> result = RestAssured.given()
                    .get("/reservations/status/" + jobId)
                    .then().statusCode(200)
                    .extract().as(Map.class);
            if (!"PENDING".equals(result.get("status"))) {
                return result;
            }
        }
        throw new RuntimeException("Job did not complete in time: " + jobId);
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

    private Long insertWaiting(String name, String date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId
        );
        return jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM waiting", Long.class
        );
    }
}
