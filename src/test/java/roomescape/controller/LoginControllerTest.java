package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.dto.request.TokenRequest;
import roomescape.dto.response.MemberResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class LoginControllerTest {

    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "1234";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("로그인 작업을 수행한다.")
    @Test
    void given_emailPassword_when_logins_then_statusCodeIsOk() {
        TokenRequest request = new TokenRequest(EMAIL, PASSWORD);
        RestAssured.given().log().all()
                .contentType("application/json")
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("인증 정보를 확인한다.")
    @Test
    void given_emailPassword_when_checkMember_then_statusCodeIsOk() {
        String accessToken = RestAssured
                .given().log().all()
                .body(new TokenRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");

        MemberResponse client = RestAssured
                .given().log().all()
                .cookies("token", accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200).extract().as(MemberResponse.class);

        assertThat(client.name()).isEqualTo("daon");
    }

    @DisplayName("로그아웃을 수행한다.")
    @Test
    void given_when_logout_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .when().post("/logout")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("이메일과 비밀번호가 유효하지 않으면 400 Bad Request를 반환한다.")
    @ParameterizedTest
    @CsvSource({"test,test", "test@naver.com,", ",test"})
    void given_InvalidInput_when_logins_then_statusCodeIsBadRequest(String email, String password) {
        TokenRequest request = new TokenRequest(email, password);
        RestAssured.given().log().all()
                .contentType("application/json")
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("모든 회원 정보를 조회가 성공하면 200 OK를 반환한다.")
    @Test
    void readMembers() {
        RestAssured.given().log().all()
                .when().get("/members")
                .then().log().all()
                .statusCode(200);
    }
}
