package roomescape.member.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LoginControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("회원가입 성공 시 USER 권한으로 응답한다")
    @Test
    void 회원가입_성공() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "신규회원", "email", "new@test.com", "password", "1234"))
                .post("/signup")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("신규회원"))
                .body("role", equalTo("USER"));
    }

    @DisplayName("로그인 성공 시 세션 쿠키가 발급된다")
    @Test
    void 로그인_성공() {
        String sessionId = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user1@test.com", "password", "1234"))
                .post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("JSESSIONID");

        assertThat(sessionId).isNotBlank();
    }

    @DisplayName("비밀번호가 틀리면 로그인에 실패한다")
    @Test
    void 로그인_실패() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user1@test.com", "password", "wrong"))
                .post("/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("로그인한 회원은 본인 프로필을 조회할 수 있다")
    @Test
    void 프로필_조회_성공() {
        String sessionId = given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user1@test.com", "password", "1234"))
                .post("/login")
                .then()
                .extract().cookie("JSESSIONID");

        given()
                .cookie("JSESSIONID", sessionId)
                .get("/member/profile")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("user1"))
                .body("email", equalTo("user1@test.com"));
    }

    @DisplayName("로그인 없이 프로필을 조회하면 401을 반환한다")
    @Test
    void 비인증_프로필_조회_실패() {
        given()
                .get("/member/profile")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
