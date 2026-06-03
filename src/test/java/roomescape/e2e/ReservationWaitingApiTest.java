package roomescape.e2e;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.exception.ProblemType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationWaitingApiTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 예약된_슬롯에_대기를_신청한다() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        createReservation("티뉴", "2026-08-05", timeId, themeId);

        Map<String, Object> request = waitingRequest("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("민욱"))
                .body("date", is("2026-08-05"))
                .body("time.id", is(timeId))
                .body("time.startAt", is("11:00"))
                .body("theme.id", is(themeId))
                .body("order", is(1));
    }

    @Test
    void 같은_예약에_대기를_신청한_순서대로_순번을_반환한다() {
        Integer timeId = createTime("12:00");
        Integer themeId = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");
        createReservation("티뉴", "2026-08-05", timeId, themeId);

        createWaiting("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingRequest("브라운", "2026-08-05", timeId, themeId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("order", is(2));
    }

    @Test
    void 같은_예약에_이미_대기를_신청했으면_409를_반환한다() {
        Integer timeId = createTime("12:00");
        Integer themeId = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");
        createReservation("티뉴", "2026-08-05", timeId, themeId);
        createWaiting("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingRequest("민욱", "2026-08-05", timeId, themeId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("type", is(ProblemType.CONFLICT.uri().toString()))
                .body("detail", is("이미 대기를 신청한 예약입니다."));
    }

    @Test
    void 내_대기_목록_조회시_대기_순번을_함께_반환한다() {
        Integer timeId = createTime("12:30");
        Integer themeId = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");
        createReservation("티뉴", "2026-08-05", timeId, themeId);

        createWaiting("브라운", "2026-08-05", timeId, themeId);
        Integer waitingId = createWaiting("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", "민욱")
                .when().get("/waitings/me")
                .then().log().all()
                .statusCode(200)
                .body("waitings.size()", is(1))
                .body("waitings[0].id", is(waitingId))
                .body("waitings[0].name", is("민욱"))
                .body("waitings[0].date", is("2026-08-05"))
                .body("waitings[0].time.id", is(timeId))
                .body("waitings[0].time.startAt", is("12:30"))
                .body("waitings[0].theme.id", is(themeId))
                .body("waitings[0].order", is(2));
    }

    @Test
    void 예약되지_않은_슬롯에는_대기를_신청할_수_없다() {
        Integer timeId = createTime("13:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingRequest("민욱", "2026-08-05", timeId, themeId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409)
                .body("type", is(ProblemType.CONFLICT.uri().toString()))
                .body("detail", is("예약된 슬롯에만 대기를 신청할 수 있습니다."));
    }

    @Test
    void 지난_예약에는_대기를_신청할_수_없다() {
        Integer timeId = createTime("13:30");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        insertPastReservation("티뉴", "2020-01-01", timeId, themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(waitingRequest("민욱", "2020-01-01", timeId, themeId))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(422)
                .body("type", is(ProblemType.BUSINESS_RULE_VIOLATION.uri().toString()))
                .body("detail", is("지난 시각에는 대기할 수 없습니다."));
    }

    @Test
    void 본인_대기를_취소하면_204를_반환한다() {
        Integer timeId = createTime("14:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        createReservation("티뉴", "2026-08-05", timeId, themeId);
        Integer waitingId = createWaiting("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", "민욱")
                .when().delete("/waitings/me/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 다른_사람_이름으로_대기를_취소하면_403을_반환한다() {
        Integer timeId = createTime("15:00");
        Integer themeId = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");
        createReservation("티뉴", "2026-08-05", timeId, themeId);
        Integer waitingId = createWaiting("민욱", "2026-08-05", timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/waitings/me/" + waitingId)
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 존재하지_않는_대기를_취소하면_404를_반환한다() {
        RestAssured.given().log().all()
                .queryParam("name", "민욱")
                .when().delete("/waitings/me/999")
                .then().log().all()
                .statusCode(404);
    }

    private Integer createWaiting(String name, String date, Integer timeId, Integer themeId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(waitingRequest(name, date, timeId, themeId))
                .when().post("/waitings")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Long insertPastReservation(String name, String date, Integer timeId, Integer themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, LocalDate.parse(date), timeId, themeId
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE name = ? AND date = ?",
                Long.class, name, LocalDate.parse(date)
        );
    }

    private Integer createReservation(String name, String date, Integer timeId, Integer themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Integer createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Integer createTheme(String name, String description, String thumbnailImageUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnailImageUrl", thumbnailImageUrl);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Map<String, Object> waitingRequest(String name, String date, Integer timeId, Integer themeId) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("date", date);
        request.put("timeId", timeId);
        request.put("themeId", themeId);
        return request;
    }
}
