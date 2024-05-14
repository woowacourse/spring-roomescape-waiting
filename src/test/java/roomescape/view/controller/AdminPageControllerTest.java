package roomescape.view.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminPageControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("관리자가 아닌 멤버는 관리자 페이지를 열 수 없다.")
    @ParameterizedTest
    @CsvSource({"/admin", "/admin/reservation", "/admin/time", "/admin/theme"})
    void loadAdminPage_whenUserLogin(String path) {
        Cookies cookies = makeUserCookie();

        RestAssured.given().log().all()
                .cookies(cookies)
                .when().get(path)
                .then().log().all()
                .statusCode(403);
    }

    private Cookies makeUserCookie() {
        LoginRequest request = new LoginRequest("bri@abc.com", "1234");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }

    @DisplayName("관리자는 관리자 페이지를 열 수 있다.")
    @ParameterizedTest
    @CsvSource({"/admin", "/admin/reservation", "/admin/time", "/admin/theme"})
    void loadAdminPage_whenAdminLogin(String path) {
        Cookies cookies = makeAdminCookie();

        RestAssured.given().log().all()
                .cookies(cookies)
                .when().get(path)
                .then().log().all()
                .statusCode(200);
    }

    private Cookies makeAdminCookie() {
        LoginRequest request = new LoginRequest("admin@abc.com", "1234");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }
}
