package roomescape.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.business.domain.Member;
import roomescape.persistence.repository.MemberRepository;
import roomescape.presentation.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LoginControllerIntegrationTest {

    @LocalServerPort
    private int port;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 로그인하면 성공 응답을 반환한다")
    void login_WithValidCredentials_ReturnsSuccess() {
        // given
        final Member member = new Member("이름", "USER", "email@test.com", "password1234!");
        memberRepository.save(member);

        final LoginRequest request = new LoginRequest("email@test.com", "password1234!");

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .cookie("token", notNullValue());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 401 상태코드를 반환한다")
    void login_WithNonExistentEmail_ReturnsBadRequest() {
        // given
        final LoginRequest request = new LoginRequest("notExistedEmail@test.com", "password");

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 401 상태코드를 반환한다")
    void login_WithInvalidPassword_ReturnsBadRequest() {
        // given
        final Member member = new Member("이름", "USER", "email@test.com", "password");
        memberRepository.save(member);

        final LoginRequest request = new LoginRequest("email@test.com", "notExistedPassword");

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("로그인 체크 시 로그인된 상태면 이름을 응답한다")
    void checkLogin_WhenLoggedIn_ReturnsTrue() {
        // given
        final Member member = new Member("이름", "USER", "email@test.com", "password");
        memberRepository.save(member);

        final LoginRequest request = new LoginRequest("email@test.com", "password");
        final String token = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/login")
                .getCookie("token");

        // when & then
        given()
                .cookie("token", token)
                .when()
                .get("/login/check")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("이름"));
    }

    @Test
    @DisplayName("로그인 체크 시 로그인되지 않은 상태면 예외가 발생한다")
    void checkLogin_WhenNotLoggedIn_ReturnsFalse() {
        given()
                .when()
                .get("/login/check")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("빈 이메일로 로그인하면 400 상태코드를 반환한다")
    void login_WithEmptyEmail_ReturnsBadRequest() {
        // given
        final String invalidRequest = """
                    {
                        email:'',
                        password:'password'
                    }
                """;

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidRequest)
                .when()
                .post("/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("빈 비밀번호로 로그인하면 400 상태코드를 반환한다")
    void login_WithEmptyPassword_ReturnsBadRequest() {
        // given
        final String invalidRequest = """
                    {
                        email:'email@test.com',
                        password:''
                    }
                """;

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidRequest)
                .when()
                .post("/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
