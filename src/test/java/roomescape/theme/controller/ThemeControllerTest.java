package roomescape.theme.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
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

    @DisplayName("인기 테마 조회 성공")
    @Test
    void 인기_테마_조회_성공() {
        given()
                .get("/themes/top/10")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(3))
                .body("[0].name", equalTo("테마A"))
                .body("[1].name", equalTo("테마B"))
                .body("[2].name", equalTo("테마C"));
    }
}
