package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeControllerTest {

    @Test
    void 전체테마_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(11));

    }

    @Test
    void 단일테마_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/themes/1")
                .then().log().all()
                .statusCode(200)
                .body("name", is("은하수"));
    }

    @Test
    void 트렌드_테마_조회_성공() throws Exception {
        RestAssured.given().log().all()
                .when().get("/themes/trending?startDate=2026-05-01&endDate=2026-05-07&limit=10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }
}
