package roomescape.auth.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.auth.web.constant.AuthConstant.AUTH_COOKIE_KEY;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.auth.dto.AuthenticatedMember;
import roomescape.auth.web.controller.dto.LoginRequest;
import roomescape.support.IntegrationTestSupport;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class AuthControllerTest extends IntegrationTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO member (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)",
                2L, "웨이드", "user@example.com", "userPassword", "USER");
    }

    @DisplayName("로그인 성공시 쿠키를 반환한다")
    @Test
    void login() {
        //given
        LoginRequest request = new LoginRequest("user@example.com", "userPassword");

        //when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().response();

        //then
        String authCookie = response.getCookie(AUTH_COOKIE_KEY);
        assertThat(authCookie).isNotBlank();
    }

    @DisplayName("로그인 이후 토큰으로 로그인 체크를 할 수 있다")
    @Test
    void loginCheck() {
        //given
        LoginRequest request = new LoginRequest("user@example.com", "userPassword");

        String authCookie = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .cookie(AUTH_COOKIE_KEY);

        //when
        AuthenticatedMember result = RestAssured.given()
                .cookie(AUTH_COOKIE_KEY, authCookie)
                .get("/login/check")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(AuthenticatedMember.class);

        //then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("user@example.com");
    }

    @DisplayName("로그아웃 시 쿠키가 만료된다")
    @Test
    void logout() {
        //given
        LoginRequest request = new LoginRequest("user@example.com", "userPassword");

        String authCookie = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .cookie(AUTH_COOKIE_KEY);

        //when
        Cookie cookie = RestAssured.given()
                .cookie(AUTH_COOKIE_KEY, authCookie)
                .post("/logout")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response().detailedCookie(AUTH_COOKIE_KEY);

        //then
        assertThat(cookie.getMaxAge()).isEqualTo(0);
    }
}
