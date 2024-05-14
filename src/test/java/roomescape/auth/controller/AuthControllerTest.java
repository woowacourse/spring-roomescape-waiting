package roomescape.auth.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.service.AuthService;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthControllerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberDao memberDao;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("로그인에 성공하면 JWT accessToken, refreshToken 을 Response 받는다.")
    void getJwtAccessTokenWhenlogin() {
        // given
        String email = "test@email.com";
        String password = "12341234";
        memberDao.insert(new Member("이름", email, password, Role.MEMBER));

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

    private String getAdminAccessTokenCookie(final String email, final String password) {
        memberDao.insert(new Member("이름", email, password, Role.ADMIN));

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

    private String getAccessTokenCookieByLogin(final String email, final String password) {
        memberDao.insert(new Member("이름", email, password, Role.ADMIN));

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
