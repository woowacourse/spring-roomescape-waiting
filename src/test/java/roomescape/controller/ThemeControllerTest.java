package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ThemeController themeController;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 인기_테마_조회_API() {
        RestAssured.given().log().all()
                .when().get("/themes/popular?limit=10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }
}
