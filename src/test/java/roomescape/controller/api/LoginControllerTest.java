package roomescape.controller.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.LoginCheckResponse;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class LoginControllerTest {

    @DisplayName("성공: 로그인 성공")
    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("admin@a.com", "123a!");
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("실패: 잘못된 이메일 혹은 잘못된 비밀번호")
    @ParameterizedTest
    @CsvSource(value = {"a@a.com,1234", "b@a.com,123!", "b@a.com,1234"})
    void login_WrongEmail_Or_WrongPassword(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("성공: 토큰을 이용해서 이름을 가져올 수 있다.")
    @Test
    void checkLogin() {
        LoginRequest request = new LoginRequest("admin@a.com", "123a!");
        String token = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all().extract().header(HttpHeaders.SET_COOKIE);

        LoginCheckResponse response = RestAssured.given().log().all()
            .header(HttpHeaders.COOKIE, token)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/login/check")
            .then().log().all()
            .statusCode(HttpStatus.OK.value()).extract().as(LoginCheckResponse.class);

        assertThat(response.name()).isEqualTo("관리자");
    }
}
