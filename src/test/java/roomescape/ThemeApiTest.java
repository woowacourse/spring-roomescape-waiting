package roomescape;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeApiTest {

    @Test
    void 테마_조회_빈목록() {
        RestAssured.given().log().all()
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(0));
    }

    @Test
    void 테마_추가_후_조회() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "추리");
        params.put("description", "단서를 찾아라");
        params.put("thumbnailImageUrl", "https://example.com/mystery.jpg");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/themes")
            .then().log().all()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", is("추리"))
            .body("description", is("단서를 찾아라"))
            .body("thumbnailImageUrl", is("https://example.com/mystery.jpg"));

        RestAssured.given().log().all()
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1))
            .body("[0].name", is("추리"));
    }

    @Test
    void 테마_추가_및_삭제() {
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
            .body("size()", is(0));
    }

    @Test
    void 예약이_존재하는_테마는_삭제할_수_없다() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer timeId = createTime("10:00");
        createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId);

        RestAssured.given().log().all()
            .when().delete("/themes/" + themeId)
            .then().log().all()
            .statusCode(409);
    }

    @Test
    void 없는_테마는_삭제할_수_없다() {
        RestAssured.given().log().all()
            .when().delete("/themes/" + 1)
            .then().log().all()
            .statusCode(404);
    }

    @Test
    void 잘못된_요청이면_400() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "");
        params.put("description", "우주에서 탈출");
        params.put("thumbnailImageUrl", "https://example.com/sf.jpg");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/themes")
            .then().log().all()
            .statusCode(400);
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 인기_테마_탑10_조회() {
        RestAssured.given().log().all()
            .queryParam("days", 7)
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(10))
            .body("name", contains(
                "미스터리 저택",
                "해적선의 보물",
                "마법사의 탑",
                "좀비 아포칼립스",
                "고대 이집트",
                "우주 정거장",
                "시간 여행자의 실험실",
                "폐쇄 병동",
                "침몰하는 잠수함",
                "은행 금고"
            ));
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

    private void createReservation(String name, String date, Integer timeId, Integer themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201);
    }
}
