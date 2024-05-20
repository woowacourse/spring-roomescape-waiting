package roomescape.auth.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerE2ETest {

    @LocalServerPort
    int serverPort;

    @BeforeEach
    public void beforeEach() {
        RestAssured.port = serverPort;
    }

    @Test
    @DisplayName("로그인에 성공하면 Set-Cookie 헤더에 쿠키 값이 전달되는 것을 확인한다")
    void checkLogin() {
        Map<String, String> loginParams = Map.of(
                "email", "mason@test.com",
                "password", "123"
        );

        String token = RestAssured.given().log().all()
                .when().body(loginParams)
                .contentType(ContentType.JSON).post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외를 반환하는 것을 확인한다")
    void checkNotExistEmailLogin() {
        Map<String, String> loginParams = Map.of(
                "email", "jazz@test.com",
                "password", "123"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("이메일: jazz@test.com 해당하는 멤버를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("형식에 맞지 않는 이메일로 로그인하는 경우 예외를 반환하는 것을 확인한다")
    void checkInvalidEmailFormatLogin() {
        Map<String, String> loginParams = Map.of(
                "email", "jazz",
                "password", "123"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("이메일: jazz 해당하는 멤버를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("틀린 비밀번호로 로그인하는 경우 예외를 반환하는 것을 확인한다")
    void checkWrongPasswordLogin() {
        Map<String, String> loginParams = Map.of(
                "email", "mason@test.com",
                "password", "1234"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then().log().all()
                .statusCode(401)
                .body("message", equalTo("비밀번호가 틀렸습니다"));
    }

    @Test
    @DisplayName("로그인한 회원의 토큰에서 회원 정보를 반환하는 것을 확인한다")
    void checkNameFromLoginToken() {
        Map<String, String> loginParams = Map.of(
                "email", "mason@test.com",
                "password", "123"
        );

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo("메이슨"));
    }

    @Test
    @DisplayName("로그인한 회원의 쿠키가 존재하지 않는다면 예외를 반환하는 것을 확인한다")
    void checkTokenNotExists() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("로그인 되어 있지 않습니다"));
    }

    @Test
    @DisplayName("로그인한 회원의 토큰에 대한 쿠키가 존재하지 않는다면 예외를 반환하는 것을 확인한다")
    void checkCookieNotExists() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", null)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("message", equalTo("유효하지 않은 로그인 정보입니다"));
    }

    @Test
    @DisplayName("로그아웃 기능을 확인한다")
    void checkLogout() {
        Map<String, String> loginParams = Map.of(
                "email", "mason@test.com",
                "password", "123"
        );

        RestAssured.given().log().all()
                .when().body(loginParams)
                .contentType(ContentType.JSON).post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().post("/logout")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        assertThat(token).isBlank();
    }
}
