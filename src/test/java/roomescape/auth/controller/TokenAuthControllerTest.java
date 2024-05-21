package roomescape.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.controller.dto.request.LoginRequest;
import roomescape.member.controller.dto.request.SignupRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TokenAuthControllerTest {
    @BeforeEach
    public void setup() {
        SignupRequest request = new SignupRequest("brown", "brown@gmail.com", "password");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/members");
    }

    @DisplayName("로그인 요청 시 토큰을 쿠키에 담아 반환한다.")
    @Test
    void login() {
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("brown@gmail.com", "password"))
                .when()
                .post("/login")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .cookie("token");
        assertThat(token).isNotNull();
    }

    @DisplayName("회원가입하지 않은 사용자의 로그인 요청 시 400 상태 코드를 반환한다.")
    @Test
    void loginFailed() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("solar@gmail.com", "password"))
                .when()
                .post("/login")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @DisplayName("토큰을 사용해 사용자의 이름을 조회한다.")
    @Test
    void getMemberByToken() {
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("brown@gmail.com", "password"))
                .when()
                .post("/login")
                .then()
                .extract()
                .cookie("token");

        RestAssured.given()
                .cookie("token", token)
                .when()
                .get("/login/check")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("brown"));
    }

    @DisplayName("로그아웃에 성공한다.")
    @Test
    void logout() {
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("brown@gmail.com", "password"))
                .when()
                .post("/login")
                .then()
                .extract()
                .cookie("token");

        String cookie = RestAssured.given()
                .cookie("token", token)
                .when()
                .post("/logout")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .cookie("token");
        assertThat(cookie).isEmpty();
    }
}
