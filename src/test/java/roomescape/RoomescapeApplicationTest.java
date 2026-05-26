package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RoomescapeApplicationTest {
    private static final String AVAILABLE_DATE = "2099-06-01";
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.update("insert into reservation_time(start_at) values ('10:00')");
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('공포', '무서워요', 'https://zeze.com')");
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('개그', '재밌어요', 'https://zeze.com')");
    }

    @Test
    void 예약_생성_후_사용_시간_조회시_해당_시간이_제외된다() {
        int before = availableCount(AVAILABLE_DATE, 1);

        reserve("제제", AVAILABLE_DATE, 1L, 1L, 201);

        int after = availableCount(AVAILABLE_DATE, 1);
        assertThat(after).isEqualTo(before - 1);
    }

    @Test
    void 예약_없는_날짜_조회시_전체_시간이_반환된다() {
        int total = RestAssured.given()
                .when().get("/times")
                .then().statusCode(200)
                .extract().jsonPath().getList(".").size();

        int available = availableCount(AVAILABLE_DATE, 1);

        assertThat(available).isEqualTo(total);
    }

    @Test
    void 다른_테마_예약은_사용_시간_조회에_영향을_주지_않는다() {
        int before = availableCount(AVAILABLE_DATE, 1);

        reserve("제제", AVAILABLE_DATE, 1L, 2L, 201);

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
        String name = "zeze";
        String date = "2099-05-14";
        long timeId = 1L;
        long themeId = 1L;
        reserve(name, date, timeId, themeId, 201);
        reserve(name, date, timeId, themeId, 409);
    }

    @Test
    void 예약이_존재하는_시간을_지우면_409를_반환한다() {
        String name = "zeze";
        String date = "2099-05-14";
        long timeId = 1L;
        long themeId = 1L;
        reserve(name, date, timeId, themeId, 201);

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
        String past = "2020-01-01";

        reserve("zeze", past, 1L, 1L, 422);
    }

    @Test
    void 이름으로_조회시_정상적으로_반환한다() {
        reserve("zeze", "2099-05-01", 1L, 1L, 201);
        reserve("zeze", "2099-05-02", 1L, 1L, 201);
        reserve("zeze", "2099-05-03", 1L, 1L, 201);
        reserve("mingu", "2099-05-04", 1L, 1L, 201);

        RestAssured.given().params("name", "zeze")
                .when().get("/reservations")
                .then().log().all()
                .body("size()", is(3));
    }

    private int availableCount(String date, long themeId) {
        return RestAssured.given()
                .when().get("/times/available?date=" + date + "&themeId=" + themeId)
                .then().statusCode(200)
                .extract().jsonPath().getList(".").size();
    }

    private void reserve(String name, String date, Long timeId, Long themeId, int expectedStatusCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
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