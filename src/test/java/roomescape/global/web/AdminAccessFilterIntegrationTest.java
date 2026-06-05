package roomescape.global.web;

import static org.hamcrest.Matchers.containsString;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:filter"
)
@ActiveProfiles("test")
class AdminAccessFilterIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 관리자_경로에_ADMIN_권한이_아닌_요청은_403으로_막힌다() {
        RestAssured.given()
                .header("role", "USER")
                .when().get("/api/admin/reservations")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(containsString("관리자만 접근 가능합니다."));
    }

    @Test
    void 관리자_경로에_ADMIN_권한의_요청은_필터를_통과한다() {
        RestAssured.given()
                .header("role", "ADMIN")
                .when().get("/api/admin/reservations")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 일반_경로는_role_헤더가_없어도_필터에_걸리지_않는다() {
        RestAssured.given()
                .when().get("/api/themes")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
