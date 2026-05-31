package roomescape.theme.controller;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("전체 테마 목록을 조회한다.")
    @Test
    void getAll() {
        Map<String, String> body = themeBody();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].name", is("오리엔탈"))
                .body("[0].description", is("오리엔탈 설명"))
                .body("[0].imageUrl", is("https://example.com/oriental.png"));
    }

    @DisplayName("테마를 생성한다.")
    @Test
    void create() {
        Map<String, String> body = themeBody();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("오리엔탈"))
                .body("description", is("오리엔탈 설명"))
                .body("imageUrl", is("https://example.com/oriental.png"));
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void delete() {
        Map<String, String> body = themeBody();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("없는 테마 삭제 시 404 에러를 응답한다.")
    @Test
    void 없는_테마_삭제시_404_에러_응답() {
        RestAssured.given().log().all()
                .when().delete("/themes/999")
                .then().log().all()
                .statusCode(404)
                .body("code", is("THEME_NOT_FOUND"))
                .body("message", is("테마를 찾을 수 없습니다."));
    }

    private static Map<String, String> themeBody() {
        Map<String, String> body = new HashMap<>();
        body.put("name", "오리엔탈");
        body.put("description", "오리엔탈 설명");
        body.put("imageUrl", "https://example.com/oriental.png");
        return body;
    }
}
