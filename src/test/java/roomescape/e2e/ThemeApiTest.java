package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import roomescape.exception.ProblemType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class ThemeApiTest extends AbstractE2eTest {

    @Test
    void 테마가_없으면_빈_목록을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(0));
    }

    @Test
    void 테마를_추가하면_201과_생성된_테마를_반환한다() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "공포");
        params.put("description", "무서운 테마");
        params.put("thumbnailImageUrl", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("공포"))
                .body("description", is("무서운 테마"))
                .body("thumbnailImageUrl", is("https://example.com/horror.jpg"));
    }

    @Test
    void 테마를_추가한_뒤_조회하면_목록에_포함된다() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "추리");
        params.put("description", "단서를 찾아라");
        params.put("thumbnailImageUrl", "https://example.com/mystery.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(1))
                .body("themes[0].name", is("추리"));
    }

    @Test
    void 인기_테마_조회시_윈도우_내_예약수가_많은_순으로_정렬된다() {
        Integer themeA = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer themeB = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");
        Integer time = createTime("10:00");

        // 윈도우: days=7 → [오늘-7, 오늘-1]
        insertReservation("user1", LocalDate.now().minusDays(1), time, themeA);
        insertReservation("user2", LocalDate.now().minusDays(2), time, themeA); // A: 2건
        insertReservation("user3", LocalDate.now().minusDays(3), time, themeB); // B: 1건

        // 윈도우 밖
        insertReservation("user4", LocalDate.now(), time, themeB);           // 오늘 = end 다음날
        insertReservation("user5", LocalDate.now().minusDays(8), time, themeB); // start 직전

        RestAssured.given().log().all()
                .when().get("/themes/popular?days=7")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(2))
                .body("themes[0].name", is("공포"))
                .body("themes[1].name", is("추리"));
    }

    @Test
    void 인기_테마_조회시_윈도우_내_예약이_없으면_빈_목록을_반환한다() {
        Integer theme = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer time = createTime("10:00");
        // 윈도우 [오늘-7, 오늘-1] 밖
        insertReservation("user1", LocalDate.now().minusDays(30), time, theme);

        RestAssured.given().log().all()
                .when().get("/themes/popular?days=7")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(0));
    }

    @Test
    void 인기_테마_조회시_limit_파라미터로_결과_개수를_제한한다() {
        Integer themeA = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer themeB = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");
        Integer themeC = createTheme("SF", "우주에서 탈출", "https://example.com/sf.jpg");
        Integer time = createTime("10:00");
        Integer time2 = createTime("11:00");

        insertReservation("user1", LocalDate.now().minusDays(1), time, themeA);
        insertReservation("user2", LocalDate.now().minusDays(2), time, themeA);
        insertReservation("user3", LocalDate.now().minusDays(1), time, themeB);
        insertReservation("user4", LocalDate.now().minusDays(1), time2, themeB);
        insertReservation("user5", LocalDate.now().minusDays(2), time2, themeC);

        RestAssured.given().log().all()
                .when().get("/themes/popular?days=7&limit=2")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(2));
    }

    @Test
    void 인기_테마_조회시_파라미터를_생략하면_기본값으로_200을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 빈_이름으로_테마_추가하면_400() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "");
        params.put("description", "무서운 테마");
        params.put("thumbnailImageUrl", "https://example.com/horror.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .body("type", is(ProblemType.VALIDATION_ERROR.uri().toString()));
    }

    @Test
    void 테마를_추가한_뒤_삭제하면_목록에서_제거된다() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "SF");
        params.put("description", "우주에서 탈출");
        params.put("thumbnailImageUrl", "https://example.com/sf.jpg");

        Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");

        RestAssured.given().log().all()
                .when().delete("/themes/" + id)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(0));
    }

    @Test
    void 사용중인_테마를_삭제하면_422() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer timeId = createTime("10:00");
        insertReservation("브라운", LocalDate.of(2026, 8, 5), timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/themes/" + themeId)
                .then().log().all()
                .statusCode(422);
    }

    private void insertReservation(String name, LocalDate date, Integer timeId, Integer themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, reservation_status) VALUES (?, ?, ?, ?, 'CONFIRM')",
                name, date, timeId, themeId
        );
    }
}
