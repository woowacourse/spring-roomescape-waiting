package roomescape.auth.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthControllerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("로그인에 성공하면 JWT accessToken, refreshToken 을 Response 받는다.")
    void getJwtAccessTokenWhenlogin() {
        // given
        String email = "test@email.com";
        String password = "12341234";
        memberRepository.save(new Member("이름", email, password, Role.MEMBER));

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
        String email = "test@test.com";
        String password = "12341234";
        String accessTokenCookie = getAccessTokenCookieByLogin(email, password);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("cookie", accessTokenCookie)
                .when().get("/login/check")
                .then()
                .body("data.name", is("이름"));
    }

    @Test
    @DisplayName("로그인 없이, 검증요청을 보내면 401 Unauthorized 를 발생한다.")
    void checkLoginFailByNotAuthorized() {
        // given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/login/check")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("회원가입을 하면 jwt AccessToken과 RefreshToken 을 Response한다.")
    void getJwtAccessTokenWhenSignup() {
        // given
        String name = "이름";
        String email = "test@email.com";
        String password = "12341234";

        Map<String, String> signupParams = Map.of(
                "name", name,
                "email", email,
                "password", password
        );

        // when
        Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
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
        memberRepository.save(new Member(name, email, password, Role.MEMBER));

        Map<String, String> signupParams = Map.of(
                "name", name,
                "email", email,
                "password", password
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
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
        List<String> jwtTokensCookie = getJwtTokensCookie(email, password);
        String accessToken = jwtTokensCookie.get(0);
        String refreshToken = jwtTokensCookie.get(1);

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
                .port(port)
                .when().get("/login/check")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("토큰 재발급을 요청하면 새로운 AccessToken과 RefreshToken을 Response 한다.")
    void reissueToken() {
        // given
        String email = "test@email.com";
        String password = "12341234";
        List<String> jwtTokensCookie = getJwtTokensCookie(email, password);
        String accessTokenCookie = jwtTokensCookie.get(0);
        String refreshTokenCookie = jwtTokensCookie.get(1);

        Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", accessTokenCookie))
                .header(new Header("Cookie", refreshTokenCookie))
                .port(port)
                .when().get("/token-reissue")
                .then().statusCode(200).log().all()
                .extract().cookies();

        String newAccessToken = cookies.get("accessToken");
        String newRefreshToken = cookies.get("accessToken");

        String olderAccessToken = accessTokenCookie.substring("accessToken=".length());
        String olderRefreshToken = accessTokenCookie.substring("refreshToken=".length());

        // then
        Assertions.assertThat(newAccessToken).isNotEqualTo(olderRefreshToken);
        Assertions.assertThat(newRefreshToken).isNotEqualTo(olderRefreshToken);
    }

    private List<String> getJwtTokensCookie(final String email, final String password) {
        memberRepository.save(new Member("이름", email, password, Role.ADMIN));

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login");

        String accessToken = response
                .then().log().all().extract().cookie("accessToken");
        String refreshToken = response
                .then().log().all().extract().cookie("refreshToken");

        List<String> tokens = new ArrayList<>();
        tokens.add("accessToken=" + accessToken);
        tokens.add("refreshToken=" + refreshToken);
        return tokens;
    }

    private String getAccessTokenCookieByLogin(final String email, final String password) {
        memberRepository.save(new Member("이름", email, password, Role.ADMIN));

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        String accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login")
                .then().log().all().extract().cookie("accessToken");

        return "accessToken=" + accessToken;
    }
}
