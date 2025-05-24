package roomescape.integration.api.rest;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.RestAssuredTestBase;
import roomescape.integration.api.RestLoginMember;

class ThemeRestTest extends RestAssuredTestBase {

    private RestLoginMember restLoginMember;
    private final Map<String, String> createThemeRequest = Map.of(
            "name", "공포방탈출",
            "description", "무서운 분위기 속에서 탈출",
            "thumbnail", "https://example.com/horror.jpg"
    );

    @BeforeEach
    void setUp() {
        restLoginMember = generateLoginMember();
    }

    @Test
    void 테마를_생성한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .body(createThemeRequest)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("공포방탈출"))
                .body("description", is("무서운 분위기 속에서 탈출"))
                .body("thumbnail", is("https://example.com/horror.jpg"));
    }

    @Test
    void 테마_목록을_조회한다() {
        테마를_생성한다();
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", is(1))
                .body("[0].name", is("공포방탈출"))
                .body("[0].description", is("무서운 분위기 속에서 탈출"))
                .body("[0].thumbnail", is("https://example.com/horror.jpg"));
    }

    @Test
    void 테마를_삭제한다() {
        Integer id = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .body(createThemeRequest)
                .when().post("/themes")
                .then().statusCode(201)
                .extract().path("id");
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().delete("/themes/{id}", id)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 인기_테마를_조회한다() {
        테마를_생성한다();
        RestAssured.given().log().all()
                .cookie("JSESSIONID", restLoginMember.sessionId())
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }
}
