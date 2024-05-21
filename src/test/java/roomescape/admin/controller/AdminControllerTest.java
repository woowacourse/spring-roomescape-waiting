package roomescape.admin.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.service.TokenCookieService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/recreate_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("어드민 컨트롤러")
public class AdminControllerTest {

    @LocalServerPort
    private int port;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        Map<String, String> body = new HashMap<>();
        body.put("email", "admin@gmail.com");
        body.put("password", "password");

        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.SET_COOKIE)
                .split(";")[0]
                .split(TokenCookieService.COOKIE_TOKEN_KEY + "=")[1];
    }

    @DisplayName("어드민 컨트롤러는 관리자가 아닌 사용자 /admin/**에 접근하는 경우 예외가 발생한다.")
    @Test
    void readAdminPageNotAdminUser() {
        Map<String, String> body = new HashMap<>();
        body.put("email", "test@gmail.com");
        body.put("password", "password");

        accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.SET_COOKIE)
                .split(";")[0]
                .split(TokenCookieService.COOKIE_TOKEN_KEY + "=")[1];

        String detailMessage = RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403)
                .extract()
                .body().jsonPath().get("detail");

        assertThat(detailMessage).isEqualTo("허용되지 않는 사용자입니다.");
    }

    @DisplayName("어드민 컨트롤러는 /admin으로 GET 요청이 들어오면 어드민 페이지를 반환한다.")
    @Test
    void readAdminPage() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200)
                .body("html.body.div.h2", equalTo("방탈출 어드민"));
    }

    @DisplayName("어드민 컨트롤러는 /admin/reservation으로 GET 요청이 들어오면 예약 목록을 페이지를 반환한다.")
    @Test
    void readReservations() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200)
                .body("html.body.div.h2", equalTo("방탈출 예약 페이지"));
    }

    @DisplayName("어드민 컨트롤러는 /admin/time으로 GET 요청이 들어오면 시간 목록을 페이지를 반환한다.")
    @Test
    void readTimes() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200)
                .body("html.body.div.h2", equalTo("시간 관리 페이지"));
    }

    @DisplayName("어드민 컨트롤러는 /admin/theme로 GET 요청이 들어오면 테마 목록 페이지를 반환한다.")
    @Test
    void readTheme() {
        RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, accessToken)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200)
                .body("html.body.div.h2", equalTo("테마 관리 페이지"));
    }
}
