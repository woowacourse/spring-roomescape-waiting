package roomescape.auth;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("로그인 성공 시, Set-Cookie 헤더에 쿠키 값을 전달한다.")
    void login() {
        jdbcTemplate.update(
                "INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("로그인을 시도한 이메일이 존재하지 않을 경우, 예외를 반환한다.")
    void login_WhenMemberNotExist() {
        // jdbcTemplate.update("INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("로그인하려는 계정이 존재하지 않습니다. 회원가입 후 로그인해주세요."));
    }

    @Test
    @DisplayName("로그인을 시도한 이메일이 올바르지 않을 경우, 예외를 반환한다.")
    void login_WhenEmailIsNull() {
         jdbcTemplate.update("INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("email", null);
        params.put("password", "hihi");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("이메일은 공백 문자가 불가능합니다."));
    }

    @Test
    @DisplayName("로그인을 시도한 이메일이 형식에 맞지 않는 경우, 예외를 반환한다.")
    void login_WhenEmailIsInvalidType() {
         jdbcTemplate.update("INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("email", "null");
        params.put("password", "hihi");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("이메일은 메일 형식만 가능합니다."));
    }

    @Test
    @DisplayName("로그인을 시도한 비밀번호가 올바르지 않을 경우, 예외를 반환한다.")
    void login_WhenPasswordNotCorrect() {
        jdbcTemplate.update(
                "INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi123");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("아이디 또는 비밀번호를 잘못 입력했습니다. 다시 입력해주세요."));
    }

    @Test
    @DisplayName("로그인을 시도한 비밀번호가 올바르지 않을 경우, 예외를 반환한다.")
    void login_WhenPasswordIsNull() {
        jdbcTemplate.update("INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@namil.cm");
        params.put("password", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("비밀번호는 공백 문자가 불가능합니다."));
    }

    @Test
    @DisplayName("로그인한 회원의 정보 조회 시, 토큰으로부터 회원 정보를 확인 후 결과를 반환한다.")
    void loginCheck() {
        // give
        jdbcTemplate.update(
                "INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo("몰리"));
    }

    @Test
    @DisplayName("로그인한 회원의 정보 조회 시, 쿠키가 존재하지 않는다면 예외를 반환한다.")
    void loginCheck_WhenCookieNotExist() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("detail", equalTo("쿠키가 없어서 회원 정보를 찾을 수 없습니다. 다시 로그인해주세요."));
    }

    @Test
    @DisplayName("로그인한 회원의 정보 조회 시, 토큰에 대한 쿠키가 존재하지 않는다면 예외를 반환한다.")
    void loginCheck_WhenTokenNotExist() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("tokenToken", null)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("detail", equalTo("토큰에 대한 쿠키가 없어서 회원 정보를 찾을 수 없습니다. 다시 로그인해주세요."));
    }
}

