package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {
    @Autowired
    private ThemeController themeController;

    @Test
    void 예약_가능한_시간_조회_API() {
        // when & then
        RestAssured.given().log().all()
                .when().get("/themes/1/available-times?date=2026-05-01")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));
    }

    @Test
    @DisplayName("최근 1주동안 예약이 많았던 테마를 조회하는 정상 테스트")
    void 테마_조회_API() {
        // when & then
        RestAssured.given().log().all()
                .when().get("/themes/popular?limit=10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }
}
