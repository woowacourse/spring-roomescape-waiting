package roomescape.acceptance;

import static org.hamcrest.Matchers.containsString;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.support.BaseIntegrationTest;

public class AuthAcceptanceTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 관리자_권한_경로일_때_잘못된_인증_정보라면_403() {
        RestAssured.given()
                .header("role", "USER")
                .when().get("/api/admin/test")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body(containsString("관리자만 접근 가능합니다."));
    }

    @Test
    void 관리자_권한_경로일_때_유효한_인증_정보라면_200() {
        RestAssured.given()
                .header("role", "ADMIN")
                .when().get("/api/admin/test")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 일반_경로에서는_헤더가_없어도_200() {
        RestAssured.given()
                .when().get("/api/client")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
