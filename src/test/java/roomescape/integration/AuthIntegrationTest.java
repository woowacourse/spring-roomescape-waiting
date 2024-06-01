package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.support.dto.TokenCookieDto;

import java.util.Map;

import static org.hamcrest.Matchers.is;

public class AuthIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("로그인에 성공하면 JWT accessToken, refreshToken 을 Response 받는다.")
    void getJwtAccessTokenWhenlogin() {
        // given
        String email = "test@email.com";
        String password = "12341234";
        memberFixture.createMember(email, password);

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        // when
        Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login")
                .then().log().all().extract().cookies();

        // then
        Assertions.assertThat(cookies.get("accessToken")).isNotNull();
        Assertions.assertThat(cookies.get("refreshToken")).isNotNull();
    }

    @Test
    @DisplayName("로그인 검증 시, 회원의 name을 응답 받는다.")
    void checkLogin() {
        // given
        String email = "test@email.com";
        String password = "12341234";
        Member member = memberFixture.createMember(email, password);
        TokenCookieDto tokenCookieDto = cookieProvider.loginAndGetTokenCookies(email, password, port);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("cookie", tokenCookieDto.accessTokenCookie())
                .when().get("/login/check")
                .then()
                .body("data.name", is(member.getName()));
    }

    @Test
    @DisplayName("로그인 없이, 검증요청을 보내면 401 Unauthorized 를 발생한다.")
    void checkLoginFailByNotAuthorized() {
        // given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("회원가입을 하면 jwt AccessToken과 RefreshToken 을 Response한다.")
    void getJwtAccessTokenWhenSignup() {
        // given
        Map<String, String> signupParams = Map.of(
                "name", "이름",
                "email", "test@email.com",
                "password", "12341234"
        );

        // when
        Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signupParams)
                .when().post("/signup")
                .then().statusCode(200).log().all()
                .extract().cookies();

        // then
        Assertions.assertThat(cookies.get("accessToken")).isNotNull();
        Assertions.assertThat(cookies.get("refreshToken")).isNotNull();
    }

    @Test
    @DisplayName("회원가입 시, 이미 요청한 Email로 가입된 회원이 있으면 409 Conflict 를 Response 한다.")
    void failSignupByEmailDuplicate() {
        // given
        String name = "이름";
        String email = "test@email.com";
        String password = "12341234";
        memberFixture.createMember(email, password);


        Map<String, String> signupParams = Map.of(
                "name", name,
                "email", email,
                "password", password
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signupParams)
                .when().post("/signup")
                .then().statusCode(409).log().all();
    }

    @Test
    @DisplayName("로그아웃을 하면 현재 토큰을 만료된 토큰으로 변경하여 Response한다.")
    void logout() {
        // given
        String email = "test@email.com";
        String password = "12341234";
        TokenCookieDto tokenCookieDto = cookieProvider.saveMemberAndGetJwtTokenCookies(email, password, port);
        String accessToken = tokenCookieDto.accessTokenCookie();
        String refreshToken = tokenCookieDto.refreshTokenCookie();

        String expiredAccessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", accessToken))
                .header(new Header("Cookie", refreshToken))
                .port(port)
                .when().post("/logout")
                .then().log().all().extract().cookie("accessToken");
        expiredAccessToken = null; // 웹 상에서는 JWT Cookie MaxAge를 0으로 만들어서 response 해주기 때문에 사라짐

        // then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", "accessToken=" + expiredAccessToken))
                .when().get("/login/check")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("토큰 재발급을 요청하면 새로운 AccessToken과 RefreshToken을 Response 한다.")
    void reissueToken() {
        // given
        String email = "test@email.com";
        String password = "12341234";
        TokenCookieDto tokenCookieDto = cookieProvider.saveMemberAndGetJwtTokenCookies(email, password, port);
        String oldAccessToken = tokenCookieDto.accessTokenCookie();
        String oldRefreshToken = tokenCookieDto.refreshTokenCookie();

        Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", oldAccessToken))
                .header(new Header("Cookie", oldRefreshToken))
                .when().get("/token-reissue")
                .then().statusCode(200).log().all()
                .extract().cookies();
        String newAccessToken = cookies.get("accessToken");
        String newRefreshToken = cookies.get("accessToken");

        // then
        Assertions.assertThat(newAccessToken).isNotEqualTo(oldRefreshToken);
        Assertions.assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
    }
}
