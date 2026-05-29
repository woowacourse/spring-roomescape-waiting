package roomescape.theme.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("인기 테마 조회 성공")
    void 인기_테마_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/themes/top/10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0].name", equalTo("테마A"));
    }

    @Test
    @DisplayName("인기 테마 조회 성공 - limit 적용")
    void 인기_테마_조회_limit_적용() {
        RestAssured.given().log().all()
                .when().get("/themes/top/2")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}
