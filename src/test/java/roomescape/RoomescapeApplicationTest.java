package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.controller.dto.response.ReservationTimeResponses;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RoomescapeApplicationTest {
    private static final String AVAILABLE_DATE = "2099-06-01";
    private static final long ZEZE_ID = 1L;
    private static final long MINGU_ID = 2L;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @org.springframework.boot.test.web.server.LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = port;
        jdbcTemplate.update("insert into reservation_time(start_at) values ('10:00')");
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('공포', '무서워요', 'https://zeze.com')");
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('개그', '재밌어요', 'https://zeze.com')");
        jdbcTemplate.update("insert into member(name) values ('zeze')");   // id=1
        jdbcTemplate.update("insert into member(name) values ('mingu')");  // id=2
    }

    @Test
    void 예약_생성_후_사용_시간_조회시_해당_시간이_제외된다() {
        int before = availableCount(AVAILABLE_DATE, 1);

        reserve(ZEZE_ID, AVAILABLE_DATE, 1L, 1L, 201);

        int after = availableCount(AVAILABLE_DATE, 1);
        assertThat(after).isEqualTo(before - 1);
    }

    @Test
    void 예약_없는_날짜_조회시_전체_시간이_반환된다() {
        int total = RestAssured.given()
                .when().get("/times")
                .then().statusCode(200).extract()
                .as(ReservationTimeResponses.class)
                .getTimes().size();

        int available = availableCount(AVAILABLE_DATE, 1);

        assertThat(available).isEqualTo(total);
    }

    @Test
    void 다른_테마_예약은_사용_시간_조회에_영향을_주지_않는다() {
        int before = availableCount(AVAILABLE_DATE, 1);

        reserve(ZEZE_ID, AVAILABLE_DATE, 1L, 2L, 201);

        int after = availableCount(AVAILABLE_DATE, 1);
        assertThat(after).isEqualTo(before);
    }

    @Test
    void 과거_날짜로_사용_시간_조회시_400을_반환한다() {
        String past = "2020-01-01";

        RestAssured.given()
                .when().get("/times/available?date=" + past + "&themeId=1")
                .then().statusCode(422);
    }

    @Test
    void themeId_없이_사용_시간_조회시_400을_반환한다() {
        RestAssured.given()
                .when().get("/times/available?date=" + AVAILABLE_DATE)
                .then().statusCode(400);
    }

    @Test
    void date_없이_가용_시간_조회시_400을_반환한다() {
        RestAssured.given()
                .when().get("/times/available?themeId=1")
                .then().statusCode(400);
    }

    @Test
    void 존재하지_않는_테마_조회시_404를_반환한다() {
        RestAssured.given()
                .when().get("/themes/999")
                .then().statusCode(404);
    }

    @Test
    void 존재하지_않는_예약_조회시_404을_반환한다() {
        RestAssured.given()
                .when().get("/reservations/999")
                .then().statusCode(404);
    }

    @Test
    void 중복_예약_수행시_409를_반환한다() {
        String date = "2099-05-14";
        reserve(ZEZE_ID, date, 1L, 1L, 201);
        reserve(ZEZE_ID, date, 1L, 1L, 409);
    }

    @Test
    void 예약이_존재하는_시간을_지우면_409를_반환한다() {
        String date = "2099-05-14";
        reserve(ZEZE_ID, date, 1L, 1L, 201);

        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all().statusCode(409);
    }

    @Test
    void 예약_가능_날짜_조회시_기준_날짜를_과거로_설정하면_422를_반환한다() {
        String past = "2020-01-01";

        RestAssured.given()
                .when().get("/times/available?date=" + past + "&themeId=1")
                .then().statusCode(422);
    }

    @Test
    void 과거_예약_생성시_422를_반환한다() {
        reserve(ZEZE_ID, "2020-01-01", 1L, 1L, 422);
    }

    @Test
    void memberId로_조회시_정상적으로_반환한다() {
        reserve(ZEZE_ID, "2099-05-01", 1L, 1L, 201);
        reserve(ZEZE_ID, "2099-05-02", 1L, 1L, 201);
        reserve(ZEZE_ID, "2099-05-03", 1L, 1L, 201);
        reserve(MINGU_ID, "2099-05-04", 1L, 1L, 201);

        RestAssured.given().params("memberId", ZEZE_ID)
                .when().get("/reservations")
                .then().log().all()
                .body("reservations.size()", is(3));
    }

    @Test
    void 예약_생성_후_단건_조회가_된다() {
        int id = reserveAndGetId(ZEZE_ID, "2099-06-01", 1L, 1L);

        RestAssured.given()
                .when().get("/reservations/" + id)
                .then().statusCode(200)
                .body("name", org.hamcrest.Matchers.equalTo("zeze"))
                .body("id", org.hamcrest.Matchers.equalTo(id));
    }

    @Test
    void 예약_생성_후_전체_목록에서_조회된다() {
        reserve(ZEZE_ID, "2099-06-01", 1L, 1L, 201);
        reserve(MINGU_ID, "2099-06-02", 1L, 1L, 201);

        RestAssured.given()
                .when().get("/reservations")
                .then().statusCode(200)
                .body("reservations.size()", is(2));
    }

    @Test
    void 첫번째_예약은_승인_상태이다() {
        int id = reserveAndGetId(ZEZE_ID, "2099-06-01", 1L, 1L);

        RestAssured.given()
                .when().get("/reservations/" + id)
                .then().statusCode(200)
                .body("state", org.hamcrest.Matchers.equalTo("승인"));
    }

    @Test
    void 같은_슬롯에_두번째_예약은_대기_상태이다() {
        String date = "2099-06-10";
        reserveAndGetId(ZEZE_ID, date, 1L, 1L);
        int waitingId = reserveAndGetId(MINGU_ID, date, 1L, 1L);

        RestAssured.given()
                .when().get("/reservations/" + waitingId)
                .then().statusCode(200)
                .body("state", org.hamcrest.Matchers.equalTo("대기"))
                .body("rank", org.hamcrest.Matchers.equalTo(1));
    }

    @Test
    void 예약_수정_성공한다() {
        int id = reserveAndGetId(ZEZE_ID, "2099-06-01", 1L, 1L);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("memberId", ZEZE_ID);
        updateParams.put("date", "2099-07-01");
        updateParams.put("timeId", 1L);
        updateParams.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().put("/reservations/" + id)
                .then().statusCode(200)
                .body("date", org.hamcrest.Matchers.equalTo("2099-07-01"));
    }

    @Test
    void 예약_삭제_성공한다() {
        int id = reserveAndGetId(ZEZE_ID, "2099-06-01", 1L, 1L);

        RestAssured.given()
                .param("memberId", ZEZE_ID)
                .when().delete("/reservations/" + id)
                .then().statusCode(204);

        RestAssured.given()
                .when().get("/reservations/" + id)
                .then().statusCode(404);
    }

    @Test
    void 예약_삭제시_회원ID가_다르면_403을_반환한다() {
        int id = reserveAndGetId(ZEZE_ID, "2099-06-01", 1L, 1L);

        RestAssured.given()
                .param("memberId", 999L)
                .when().delete("/reservations/" + id)
                .then().statusCode(403);
    }

    @Test
    void 예약_생성시_memberId가_없으면_400을_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2099-06-01");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(400);
    }

    @Test
    void 예약_생성시_timeId가_없으면_400을_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", ZEZE_ID);
        params.put("date", "2099-06-01");
        params.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(400);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_404를_반환한다() {
        reserve(ZEZE_ID, "2099-06-01", 999L, 1L, 404);
    }

    @Test
    void 존재하지_않는_테마로_예약시_404를_반환한다() {
        reserve(ZEZE_ID, "2099-06-01", 1L, 999L, 404);
    }

    private int reserveAndGetId(Long memberId, String date, Long timeId, Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private int availableCount(String date, long themeId) {
        return RestAssured.given()
                .when().get("/times/available?date=" + date + "&themeId=" + themeId)
                .then().statusCode(200).extract()
                .as(ReservationTimeResponses.class)
                .getTimes().size();
    }

    private void reserve(Long memberId, String date, Long timeId, Long themeId, int expectedStatusCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(expectedStatusCode);
    }
}
