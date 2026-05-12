package roomescape;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationApiTest {

    @Test
    void 예약_조회_빈목록() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0))
                .body("totalCount", is(0))
                .body("page", is(0))
                .body("size", is(20));
    }

    @Test
    void 예약_추가() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-08-05");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("민욱"))
                .body("date", is("2026-08-05"));
    }

    @Test
    void 예약_추가_후_조회() {
        Integer timeId = createTime("14:00");
        Integer themeId = createTheme("추리", "단서를 찾아라", "https://example.com/mystery.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "티뉴");
        params.put("date", "2026-09-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("totalCount", is(1))
                .body("reservations[0].name", is("티뉴"));
    }

    @Test
    void 예약_추가_및_삭제() {
        Integer timeId = createTime("18:00");
        Integer themeId = createTheme("SF", "우주에서 탈출", "https://example.com/sf.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-10-10");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        Integer reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0))
                .body("totalCount", is(0));
    }

    @Test
    void 페이지_크기보다_많은_예약이_있으면_size만큼만_반환된다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        for (int i = 1; i <= 5; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "사용자" + i);
            params.put("date", "2026-08-0" + i);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations");
        }

        RestAssured.given().log().all()
                .when().get("/reservations?page=0&size=3")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(3))
                .body("totalCount", is(5))
                .body("page", is(0))
                .body("size", is(3));
    }

    @Test
    void 두번째_페이지_조회시_나머지_예약이_반환된다() {
        Integer timeId = createTime("10:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        for (int i = 1; i <= 5; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "사용자" + i);
            params.put("date", "2026-08-0" + i);
            params.put("timeId", timeId);
            params.put("themeId", themeId);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations");
        }

        RestAssured.given().log().all()
                .when().get("/reservations?page=1&size=3")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2))
                .body("totalCount", is(5))
                .body("page", is(1))
                .body("size", is(3));
    }

    @Test
    void 존재하지_않는_날짜로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-02-31");
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
    void 윤년이_아닌_해의_2월_29일로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-02-29");
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
    void 범위를_벗어난_월로_예약하면_400() {
        Integer timeId = createTime("11:00");
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "민욱");
        params.put("date", "2026-13-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    private Integer createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");
    }

    private Integer createTheme(String name, String description, String thumbnailImageUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnailImageUrl", thumbnailImageUrl);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().get("id");
    }
}
