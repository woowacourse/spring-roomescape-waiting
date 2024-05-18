package roomescape.controller.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class LoginControllerTest {

    private static final String ID = "admin@a.com";
    private static final String PASSWORD = "123a!";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.update(
            "INSERT INTO member(name, email, password, role) VALUES ('관리자', ?, ?, 'ADMIN')", ID, PASSWORD);
    }

    @DisplayName("성공: 로그인 성공")
    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest(ID, PASSWORD);
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: 잘못된 이메일 혹은 잘못된 비밀번호")
    @ParameterizedTest
    @CsvSource(value = {"admin@a.com,123a!!", "admim@a.com,123a!"})
    void login_WrongEmail_Or_WrongPassword(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("성공: 토큰을 이용해서 회원 정보를 가져올 수 있다.")
    @Test
    void checkLogin() {
        LoginRequest request = new LoginRequest(ID, PASSWORD);
        String token = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all().extract().header(HttpHeaders.SET_COOKIE);

        RestAssured.given().log().all()
            .header(HttpHeaders.COOKIE, token)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/login/check")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .body("name", is("관리자"))
            .body("role", is("ADMIN"));
    }
}
