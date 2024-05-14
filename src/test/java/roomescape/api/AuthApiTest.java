package roomescape.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.LoginTestSetting.getCookieByLogin;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.login.LoginCheckResponse;
import roomescape.dto.login.LoginRequest;
import roomescape.infrastructure.JwtProvider;

@Sql("/member-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AuthApiTest {

    private static final String NAME = "테드";
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "123456";

    @Autowired
    JwtProvider jwtProvider;

    @LocalServerPort
    int port;

    @Test
    void 토근_로그인() {
        String token = RestAssured
                .given().log().all()
                .port(port)
                .body(new LoginRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");

        Long userId = Long.parseLong(jwtProvider.getSubject(token));

        assertAll(
                () -> assertThat(jwtProvider.isValidateToken(token)).isTrue(),
                () -> assertThat(userId).isEqualTo(1)
        );
    }

    @Test
    void 로그인_사용자_조회() {
        Cookie cookieByLogin = getCookieByLogin(port, EMAIL, PASSWORD);

        LoginCheckResponse response = RestAssured
                .given().log().all()
                .port(port)
                .cookie(cookieByLogin)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract().as(LoginCheckResponse.class);

        assertThat(response.name()).isEqualTo(NAME);
    }
}
