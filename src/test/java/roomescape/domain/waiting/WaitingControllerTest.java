package roomescape.domain.waiting;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
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
class WaitingControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 예약_대기_정상_생성_확인_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        insertReservation("예약자", "2099-12-31", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("SUCCESS", result.get("status"));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE name = ?", Integer.class, "유저1"
        );
        assertEquals(1, count);
    }

    @Test
    void createWaiting_이름이_비어있는경우_에러_반환_테스트() {
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
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400)
                .body("message", is("예약할 사용자의 이름은 필수 입력 값입니다."));
    }

    @Test
    void createWaiting_과거_날짜인경우_에러_반환_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        insertReservation("예약자", "2000-01-01", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2000-01-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("이미 지난 날짜의 예약은 생성할 수 없습니다.", result.get("errorMessage"));
    }

    @Test
    void createWaiting_중복된_예약인경우_에러_반환_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        insertReservation("예약자", "2099-12-31", timeId, themeId);
        insertWaiting("유저1", "2099-12-31", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("해당 이름의 예약 대기가 이미 존재합니다.", result.get("errorMessage"));
    }

    @Test
    void createWaiting_존재하지_않는_시간_id인경우_에러_반환_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", 999L);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("해당 time id를 찾을 수 없습니다.", result.get("errorMessage"));
    }

    @Test
    void createWaiting_존재하지_않는_테마_id인경우_에러_반환_테스트() throws InterruptedException {
        Long timeId = insertTime("10:00", "11:00");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "유저1");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", 999L);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("해당 theme id를 찾을 수 없습니다.", result.get("errorMessage"));
    }

    @Test
    void 특정_예약_대기_삭제_정상_동작_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        Long waitingId = insertWaiting("유저1", "2099-12-31", timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/waiting/" + waitingId)
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE id = ?", Integer.class, waitingId
        );
        assertEquals(0, count);
    }

    @Test
    void deleteWaiting_존재하지_않는_id인경우_에러_반환_테스트() {
        RestAssured.given().log().all()
                .when().delete("/reservations/waiting/999")
                .then().log().all()
                .statusCode(404)
                .body("message", is("해당 waiting id를 찾을 수 없습니다."));
    }

    @Test
    void 나의_예약_대기_조회_정상_동작_테스트() {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        insertWaiting("유저2", "2099-12-31", timeId, themeId);
        insertWaiting("유저1", "2099-12-31", timeId, themeId);
        insertWaiting("유저1", "2099-12-30", timeId, themeId);
        insertWaiting("유저2", "2099-12-30", timeId, themeId);

        RestAssured.given().log().all()
                .when().get("/reservations/waiting/mine?name=유저1")
                .then().log().all()
                .statusCode(200)
                .body("waitings.size()", is(2))
                .body("waitings[0].name", is("유저1"))
                .body("waitings[0].themeName", is("테마1"))
                .body("waitings[0].waitingNumber", is(2))
                .body("waitings[1].name", is("유저1"))
                .body("waitings[1].themeName", is("테마1"))
                .body("waitings[1].waitingNumber", is(1));
    }

    @Test
    void 나의_예약_조회_예약이_없는경우_빈_리스트_반환_테스트() {
        RestAssured.given().log().all()
                .when().get("/reservations/waiting/mine?name=없는유저")
                .then().log().all()
                .statusCode(200)
                .body("waitings", is(empty()));
    }

    @Test
    void createWaiting_예약이_없는_슬롯인경우_에러_반환_테스트() throws InterruptedException {
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
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("해당 예약은 점유되어있지 않습니다.", result.get("errorMessage"));
    }

    @Test
    void createWaiting_이미_예약한_사람인경우_에러_반환_테스트() throws InterruptedException {
        Long themeId = insertTheme("테마1");
        Long timeId = insertTime("10:00", "11:00");
        insertReservation("예약자", "2099-12-31", timeId, themeId);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "예약자");
        params.put("date", "2099-12-31");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        String jobId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(202)
                .extract().jsonPath().getString("jobId");

        Map<String, Object> result = pollUntilDone(jobId);
        assertEquals("FAILED", result.get("status"));
        assertEquals("예약이 이미 등록되어있습니다.", result.get("errorMessage"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pollUntilDone(String jobId) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            Map<String, Object> result = RestAssured.given()
                    .get("/reservations/waiting/status/" + jobId)
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

    private Long insertWaiting(String name, String date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId
        );
        return jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM waiting", Long.class
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
