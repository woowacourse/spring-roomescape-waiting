package roomescape.login;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import roomescape.login.application.JwtHandler;
import roomescape.login.application.TokenCookieService;
import roomescape.login.application.dto.LoginRequest;

@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthApiTest {

    @LocalServerPort
    private int port;

    private final JwtHandler jwtHandler;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Autowired
    public AuthApiTest(final JwtHandler jwtHandler) {
        this.jwtHandler = jwtHandler;
    }

    @Test
    void 로그인에_성공하면_JWT_accessToken을_받는다() {
        // given
        final String email = "test1@test.com";
        final String password = "1234";

        final LoginRequest request = new LoginRequest(email, password);

        // when
        final String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.SET_COOKIE)
                .split(";")[0]
                .split(TokenCookieService.COOKIE_TOKEN_KEY + "=")[1];

        final String actual = jwtHandler.decode(token, JwtHandler.CLAIM_ID_KEY);

        // then
        assertThat(actual).isEqualTo("1");
    }

    @Test
    void 잘못된_아이디로_로그인하면_예외가_발생한다() {
        // given
        final String email = "test1234@test.com";
        final String password = "1234";

        final LoginRequest request = new LoginRequest(email, password);

        // when
        final String message = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(404)
                .extract()
                .asString();

        // then
        assertThat(message).isEqualTo("회원 정보가 존재하지 않습니다.");
    }

    @Test
    void 로그인을_하면_사용자_이름을_반환한다() {
        // given
        final String email = "test1@test.com";
        final String password = "1234";

        final LoginRequest request = new LoginRequest(email, password);

        // when
        final String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.SET_COOKIE)
                .split(";")[0]
                .split(TokenCookieService.COOKIE_TOKEN_KEY + "=")[1];

        final String actual = RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath().get("name");

        // then
        assertThat(actual).isEqualTo("엠제이");
    }

    @Test
    void 잘못된_비밀번호로_로그인하면_예외가_발생한다() {
        // given
        final String email = "test1@test.com";
        final String password = "wrong_password";

        final LoginRequest request = new LoginRequest(email, password);

        // when
        final String message = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(404)
                .extract()
                .asString();

        // then
        assertThat(message).isEqualTo("회원 정보가 존재하지 않습니다.");
    }

    @Test
    void 토큰없이_로그인_체크를_하면_예외가_발생한다() {
        // given
        final String message = RestAssured.given().log().all()
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .extract()
                .asString();

        // when & then
        assertThat(message).isEqualTo("로그인이 필요합니다.");
    }

    @Test
    void 만료된_토큰으로_로그인_체크를_하면_예외가_발생한다() {
        // given
        final String expiredToken = "expired_token";

        final String message = RestAssured.given().log().all()
                .cookie(TokenCookieService.COOKIE_TOKEN_KEY, expiredToken)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .extract()
                .asString();

        // when & then
        assertThat(message).isEqualTo("로그인 정보가 유효하지 않습니다.");
    }

    @Test
    void 유효하지_않은_이메일_형식으로_로그인하면_예외가_발생한다() {
        // given
        final String invalidEmail = "invalid-email";
        final String password = "1234";

        final LoginRequest request = new LoginRequest(invalidEmail, password);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(400);
    }
}
