package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClientMemberTest {
    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("회원 가입 요청 성공 시 200을 응답한다.")
    @Test
    void given_when_signupSuccess_then_statusCodeIsOk() {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "wedge@newMember.com");
        params.put("password", "1q2w3e4r!");
        params.put("name", "웨지");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then().log().all()
                .statusCode(200);
    }


    @DisplayName("회원 가입 요청 실패 시 400을 응답한다.")
    @Test
    void given_when_signupFail_then_statusCodeIsBadRequest() {
        Map<String, Object> params = new HashMap<>();
        // 이미 있는 계정
        params.put("email", "poke@test.com");
        params.put("password", "1q2w3e4r!");
        params.put("name", "poke");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("로그인 실패 시 401을 응답한다.")
    @Test
    void given_when_loginFailed_then_statusCodeForbidden() {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "poke@test.com");
        // 비밀번호 불일치
        params.put("password", "1q2w3e4r!");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("로그인 성공 시 200 응답하고 cookie 를 설정한다.")
    @Test
    void given_when_loginSuccess_then_statusCodeOk() {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "wedge@test.com");
        params.put("password", "test1234");
        var response = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all().extract();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.header("Set-Cookie").split(";")[0]).isNotEmpty();
    }

    @DisplayName("토큰과 함께 로그인 확인 요청 시 200를 응답한다.")
    @Test
    void given_when_loginCheckSuccess_then_statusCodeOk() {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "wedge@test.com");
        params.put("password", "test1234");
        var cookies = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .extract().response().getDetailedCookies();

        RestAssured.given().log().all().cookies(cookies)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200);
    }
}
