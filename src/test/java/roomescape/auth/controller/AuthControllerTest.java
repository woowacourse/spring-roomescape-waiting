package roomescape.auth.controller;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.utils.JdbcTemplateUtils;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcTemplateUtils.deleteAllTables(jdbcTemplate);
        String sql = "insert into member (name, email, password, role) values ('어드민', 'admin@woowa.com', '12341234', 'ADMIN')";
        jdbcTemplate.update(sql);
    }

    @DisplayName("로그인 요청을 보내면 응답 쿠키에 토큰이 담겨 로그인을 성공한다.")
    @Test
    void login() {
        Map<String, String> loginParams = Map.of("email", "admin@woowa.com", "password", "12341234");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .cookie("token", any(String.class));
    }

    @DisplayName("존재하지 않는 사용자로 로그인 요청을 보내면 로그인할 수 없다.")
    @Test
    void loginWithInvalidMember() {
        Map<String, String> invalidLoginParams = Map.of("email", "invalid@woowa.com", "password", "12341234");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(invalidLoginParams)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("로그인 이후 로그인 상태를 확인한다.")
    @Test
    void loginCheck() {
        Map<String, String> loginParams = Map.of("email", "admin@woowa.com", "password", "12341234");
        String tokenValue = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then()
                .extract().cookie("token");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", tokenValue)
                .when().get("/login/check")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", is("어드민"));
    }

    @DisplayName("로그아웃 요청을 보내면 쿠키의 토큰 값을 지운다.")
    @Test
    void logout() {
        Map<String, String> loginParams = Map.of("email", "admin@woowa.com", "password", "12341234");
        String tokenValue = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then()
                .extract().cookie("token");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", tokenValue)
                .when().post("/logout")
                .then()
                .statusCode(HttpStatus.OK.value())
                .cookies("token", is(""));
    }
}
